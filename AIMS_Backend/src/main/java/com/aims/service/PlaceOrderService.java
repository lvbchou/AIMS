package com.aims.service;

import com.aims.constants.OrderStatusValues;
import com.aims.dto.order.CartValidationResult;
import com.aims.dto.order.InvoiceLineItemDTO;
import com.aims.dto.order.InvoiceScreenDTO;
import com.aims.dto.order.ValidatedDeliveryInfo;
import com.aims.dto.order.CalculateShippingRequest;
import com.aims.dto.order.CartItemRequest;
import com.aims.dto.order.CreateInvoiceRequest;
import com.aims.dto.order.DeliveryInfoRequest;
import com.aims.dto.order.PlaceOrderRequest;
import com.aims.dto.order.InvoiceLineResponse;
import com.aims.dto.order.InvoiceResponse;
import com.aims.dto.order.StockAvailabilityIssue;
import com.aims.entity.Delivery;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.entity.OrderItem;
import com.aims.entity.TransactionStatus;
import com.aims.entity.product.Product;
import com.aims.exception.InsufficientStockException;
import com.aims.exception.InvalidOrderException;
import com.aims.repository.DeliveryRepository;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderItemRepository;
import com.aims.repository.OrderRepository;
import com.aims.repository.PaymentTransactionRepository;
import com.aims.repository.product.ProductRepository;
import com.aims.service.notification.NotificationService;
import com.aims.service.shipping.ShippingFeeRequest;
import com.aims.service.shipping.ShippingFeeResult;
import com.aims.service.shipping.ShippingFeeService;
import com.aims.service.shipping.ShippingItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceOrderService {
    private static final double VAT_RATE = 0.10;
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ShippingFeeService shippingFeeService;
    private final NotificationService notificationService;

    public void placeOrder(PlaceOrderRequest placeOrderRequest) {
        validateAndBuildCartContext(placeOrderRequest == null ? null : placeOrderRequest.getItems());
    }

    public Long calculateShipping(CalculateShippingRequest request) {
        if (request == null || request.getDeliveryProvince() == null
                || request.getDeliveryProvince().trim().isEmpty()) {
            throw new InvalidOrderException("Delivery province is required to calculate shipping fee.");
        }

        CartValidationResult cart = validateAndBuildCartContext(request.getItems());
        ShippingFeeResult shippingFee = shippingFeeService.calculate(new ShippingFeeRequest(
                request.getDeliveryProvince(),
                cart.getSubtotalExVat(),
                cart.getShippingItems()));
        return shippingFee.shippingFee();
    }

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        if (request == null) {
            throw new InvalidOrderException("Invoice request is required.");
        }

        ValidatedDeliveryInfo deliveryInfo = validateDeliveryInfo(request.getDeliveryInfo());
        CartValidationResult cart = validateAndBuildCartContext(request.getItems());

        long vat = Math.round(cart.getSubtotalExVat() * VAT_RATE);
        long subtotalIncVat = cart.getSubtotalExVat() + vat;
        ShippingFeeResult shippingFee = shippingFeeService.calculate(new ShippingFeeRequest(
                deliveryInfo.getDeliveryProvince(),
                cart.getSubtotalExVat(),
                cart.getShippingItems()));

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setStatus(OrderStatusValues.AWAITING_PAYMENT);
        order = orderRepository.saveAndFlush(order);
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
            throw new InvalidOrderException("Cannot create delivery because order ID was not generated.");
        }

        for (int i = 0; i < cart.getProducts().size(); i++) {
            Product product = cart.getProducts().get(i);
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cart.getQuantities().get(i));
            orderItemRepository.save(orderItem);
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(order.getOrderId());
        delivery.setRecipientName(deliveryInfo.getRecipientName());
        delivery.setPhoneNumber(deliveryInfo.getPhoneNumber());
        delivery.setEmail(deliveryInfo.getEmail());
        delivery.setDeliveryProvince(deliveryInfo.getDeliveryProvince());
        delivery.setDetailAddress(deliveryInfo.getDetailAddress());
        delivery.setNote(deliveryInfo.getNote());
        deliveryRepository.saveAndFlush(delivery);

        Invoice invoice = new Invoice(order);
        invoice.setSubTotalExVAT(cart.getSubtotalExVat());
        invoice.setSubTotalIncVAT(subtotalIncVat);
        invoice.setShippingFee(shippingFee.shippingFee());
        invoice = invoiceRepository.save(invoice);

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .orderId(invoice.getOrder().getOrderId())
                .issueDate(invoice.getIssueDate())
                .items(cart.getInvoiceItems())
                .subtotalExVAT(cart.getSubtotalExVat())
                .vat(vat)
                .subtotalIncVAT(subtotalIncVat)
                .shippingFee(shippingFee.shippingFee())
                .total(subtotalIncVat + shippingFee.shippingFee())
                .build();
    }

    @Transactional(readOnly = true)
    public InvoiceScreenDTO getInvoiceScreen(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new InvalidOrderException("Order ID is required.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Order does not exist. ID: " + orderId));
        if (!OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus())) {
            throw new InvalidOrderException("Only pending payment orders can be viewed as an invoice.");
        }

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvalidOrderException("Invoice does not exist for order ID: " + orderId));
        Delivery delivery = deliveryRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException(
                        "Delivery information does not exist for order ID: " + orderId));

        List<OrderItem> orderItems = orderItemRepository.findAllWithProductByOrderId(orderId);
        List<InvoiceLineItemDTO> invoiceLines = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            invoiceLines.add(InvoiceLineItemDTO.builder()
                    .productId(product.getProductId())
                    .productTitle(product.getTitle())
                    .category(product.getCategory())
                    .image(product.getImage())
                    .quantity(orderItem.getQuantity())
                    .unitSellingPrice(product.getSellingPrice())
                    .lineTotalSellingPrice(product.getSellingPrice() * orderItem.getQuantity())
                    .build());
        }

        return InvoiceScreenDTO.builder()
                .orderId(orderId)
                .invoiceId(invoice.getInvoiceId())
                .issueDate(invoice.getIssueDate())
                .lineItems(invoiceLines)
                .totalProductPriceExclVat(invoice.getSubTotalExVAT())
                .totalProductPriceInclVat(invoice.getSubTotalIncVAT())
                .deliveryFee(invoice.getShippingFee())
                .totalAmountToBePaid(invoice.getSubTotalIncVAT() + invoice.getShippingFee())
                .recipientName(delivery.getRecipientName())
                .phoneNumber(delivery.getPhoneNumber())
                .email(delivery.getEmail())
                .detailAddress(delivery.getDetailAddress())
                .province(delivery.getDeliveryProvince())
                .build();
    }

    @Transactional
    public InvoiceResponse confirmPaidOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new InvalidOrderException("Order ID is required.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Order does not exist. ID: " + orderId));
        if (!OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus())) {
            throw new InvalidOrderException("Only pending orders can be confirmed as paid.");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);
        if (orderItems.isEmpty()) {
            throw new InvalidOrderException("Order has no items.");
        }

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvalidOrderException("Invoice does not exist for order ID: " + orderId));

        paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.success)
                .orElseThrow(() -> new InvalidOrderException("Payment has not been completed for order ID: " + orderId));

        List<InvoiceLineResponse> invoiceItems = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.findWithLockByProductId(orderItem.getProduct().getProductId())
                    .orElseThrow(() -> new InvalidOrderException(
                            "Product does not exist. ID: " + orderItem.getProduct().getProductId()));
            if (orderItem.getQuantity() <= 0) {
                throw new InvalidOrderException("Ordered quantity must be greater than zero.");
            }
            if (!"active".equalsIgnoreCase(product.getStatus())) {
                throw new InvalidOrderException("Product '" + product.getTitle() + "' is not available for sale.");
            }
            if (orderItem.getQuantity() > product.getQuantityInStock()) {
                throw new InvalidOrderException("Product '" + product.getTitle() + "' only has "
                        + product.getQuantityInStock() + " item(s) in stock, but "
                        + orderItem.getQuantity() + " were ordered.");
            }

            product.setQuantityInStock(product.getQuantityInStock() - orderItem.getQuantity());
            productRepository.save(product);
            invoiceItems.add(InvoiceLineResponse.builder()
                    .productId(product.getProductId())
                    .title(product.getTitle())
                    .category(product.getCategory())
                    .image(product.getImage())
                    .quantity(orderItem.getQuantity())
                    .unitPriceExVat(product.getSellingPrice())
                    .amountExVat(product.getSellingPrice() * orderItem.getQuantity())
                    .build());
        }

        order.setStatus(OrderStatusValues.PENDING_PROCESSING);
        orderRepository.save(order);
        notificationService.sendOrderSuccessNotification(orderId);

        long subtotalExVat = invoice.getSubTotalExVAT();
        long vat = invoice.getSubTotalIncVAT() - subtotalExVat;

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .orderId(invoice.getOrder().getOrderId())
                .issueDate(invoice.getIssueDate())
                .items(invoiceItems)
                .subtotalExVAT(subtotalExVat)
                .vat(vat)
                .subtotalIncVAT(invoice.getSubTotalIncVAT())
                .shippingFee(invoice.getShippingFee())
                .total(invoice.getSubTotalIncVAT() + invoice.getShippingFee())
                .build();
    }

    private CartValidationResult validateAndBuildCartContext(List<CartItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Your cart is empty.");
        }

        long subtotalExVat = 0;
        List<Product> products = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        List<InvoiceLineResponse> invoiceItems = new ArrayList<>();
        List<ShippingItem> shippingItems = new ArrayList<>();
        List<StockAvailabilityIssue> affectedItems = new ArrayList<>();

        for (CartItemRequest item : items) {
            if (item == null || item.getProductId() == null || item.getQuantity() == null) {
                throw new InvalidOrderException("Cart item must include productId and quantity.");
            }
            if (item.getQuantity() <= 0) {
                throw new InvalidOrderException("Ordered quantity must be greater than zero.");
            }

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new InvalidOrderException(
                            "Product does not exist. ID: " + item.getProductId()));
            if (!"active".equalsIgnoreCase(product.getStatus())) {
                throw new InvalidOrderException("Product '" + product.getTitle() + "' is not available for sale.");
            }
            if (item.getQuantity() > product.getQuantityInStock()) {
                affectedItems.add(StockAvailabilityIssue.builder()
                        .productId(product.getProductId())
                        .title(product.getTitle())
                        .requestedQuantity(item.getQuantity())
                        .availableQuantity(product.getQuantityInStock())
                        .build());
                continue;
            }

            products.add(product);
            quantities.add(item.getQuantity());
            subtotalExVat += product.getSellingPrice() * item.getQuantity();
            shippingItems.add(new ShippingItem(
                    product.getProductId(),
                    item.getQuantity(),
                    product.getWeight(),
                    product.getDimensions()));
            invoiceItems.add(InvoiceLineResponse.builder()
                    .productId(product.getProductId())
                    .title(product.getTitle())
                    .category(product.getCategory())
                    .image(product.getImage())
                    .quantity(item.getQuantity())
                    .unitPriceExVat(product.getSellingPrice())
                    .amountExVat(product.getSellingPrice() * item.getQuantity())
                    .build());
        }

        if (!affectedItems.isEmpty()) {
            throw new InsufficientStockException(affectedItems);
        }

        return new CartValidationResult(subtotalExVat, products, quantities, invoiceItems, shippingItems);
    }

    private ValidatedDeliveryInfo validateDeliveryInfo(DeliveryInfoRequest deliveryInfoRequest) {
        if (deliveryInfoRequest == null) {
            throw new InvalidOrderException("Delivery information is required.");
        }
        if (deliveryInfoRequest.getRecipientName() == null || deliveryInfoRequest.getRecipientName().trim().isEmpty()
                || deliveryInfoRequest.getPhoneNumber() == null || deliveryInfoRequest.getPhoneNumber().trim().isEmpty()
                || deliveryInfoRequest.getDeliveryProvince() == null
                || deliveryInfoRequest.getDeliveryProvince().trim().isEmpty()
                || deliveryInfoRequest.getDetailAddress() == null
                || deliveryInfoRequest.getDetailAddress().trim().isEmpty()) {
            throw new InvalidOrderException("Recipient name, phone number, province, and detailed address are required.");
        }
        if (!deliveryInfoRequest.getPhoneNumber().trim().matches("\\d{10}")) {
            throw new InvalidOrderException("Phone number must contain exactly 10 digits.");
        }

        String email = deliveryInfoRequest.getEmail();
        if (email != null && email.trim().isEmpty()) {
            email = null;
        }
        if (email != null && !email.trim().matches(EMAIL_PATTERN)) {
            throw new InvalidOrderException("Email address is invalid.");
        }

        String note = deliveryInfoRequest.getNote();
        return new ValidatedDeliveryInfo(
                deliveryInfoRequest.getRecipientName().trim(),
                deliveryInfoRequest.getPhoneNumber().trim(),
                email == null ? null : email.trim(),
                deliveryInfoRequest.getDeliveryProvince().trim(),
                deliveryInfoRequest.getDetailAddress().trim(),
                note == null || note.trim().isEmpty() ? null : note.trim());
    }
}

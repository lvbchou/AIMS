/*
 * LAB12 SOLID REVIEW:
 * Violated principles:
 * - SRP: this class handles session cart retrieval, request-to-domain
 *   conversion, product availability validation, VAT/shipping calculations,
 *   order/delivery/invoice persistence, invoice response mapping, and stock
 *   deduction. The createInvoice method also validates input, computes prices,
 *   creates persisted domain objects, and maps the response. The confirmPaidOrder
 *   method mixes payment-state checks, inventory update, order status update,
 *   session cleanup, and response mapping.
 * - OCP: pricing and shipping rules are hard-coded in private methods and
 *   constants. VAT, free-shipping discount, province classification, base
 *   shipping fee, and weight-step fee are embedded in this class. Adding rush
 *   delivery, a new province group, a new discount, or a different VAT policy
 *   requires modifying this class.
 * - DIP: the implementation depends directly on HttpSession and concrete JPA
 *   repositories instead of smaller application ports for cart storage, product
 *   lookup, pricing, shipping, inventory, and persistence orchestration.
 * Impact: the class becomes difficult to test in isolation, has high change
 * risk, and creates merge conflicts when several team members work on shared
 * Place Order behavior.
 * Improvement directions:
 * - SRP solution: extract CartSessionAdapter, ProductAvailabilityService,
 *   OrderCreationService, InventoryService, OrderStatusService, and InvoiceMapper
 *   so each class has one clear reason to change.
 * - OCP solution: move VAT, discount, and shipping formulas behind PricingService,
 *   VatPolicy, DiscountPolicy, and ShippingFeePolicy strategies so new pricing
 *   rules can be added without editing this class.
 * - DIP solution: depend on application-level ports such as CartStore,
 *   ProductCatalogPort, OrderPersistencePort, InvoicePersistencePort, and
 *   InventoryPort instead of HttpSession and concrete Spring Data repositories.
 * Keep this class as a small orchestration service that depends on abstractions.
 */
package com.aims.service.impl;

import com.aims.dto.Cart;
import com.aims.dto.CartItem;
import com.aims.dto.request.CartItemRequest;
import com.aims.dto.request.DeliveryInfoRequest;
import com.aims.dto.request.PlaceOrderRequest;
import com.aims.dto.response.InvoiceLineResponse;
import com.aims.dto.response.InvoiceResponse;
import com.aims.entity.Delivery;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.entity.OrderItem;
import com.aims.entity.OrderItemId;
import com.aims.entity.product.Product;
import com.aims.exception.InvalidOrderException;
import com.aims.repository.DeliveryRepository;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderItemRepository;
import com.aims.repository.OrderRepository;
import com.aims.repository.ProductRepository;
import com.aims.service.PlaceOrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceOrderServiceImpl implements PlaceOrderService {
    private static final String CART_SESSION_KEY = "cart";
    private static final double VAT_RATE = 0.10;
    private static final long FREE_SHIPPING_THRESHOLD = 100_000L;
    private static final long MAX_FREE_SHIPPING_DISCOUNT = 25_000L;

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public void processOrder(PlaceOrderRequest placeOrderRequest, HttpSession session) {
        Cart cart = resolveCart(placeOrderRequest, session, true);
        validateCartAvailability(cart, false);
    }

    @Override
    public Long calculateShippingFee(DeliveryInfoRequest deliveryInfoRequest, HttpSession session) {
        validateDeliveryInfoForShipping(deliveryInfoRequest);
        Cart cart = getSessionCart(session);
        OrderPricing pricing = calculatePricing(cart, deliveryInfoRequest);
        return pricing.shippingFee();
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(DeliveryInfoRequest deliveryInfoRequest, HttpSession session) {
        validateDeliveryInfoForInvoice(deliveryInfoRequest);
        Cart cart = getSessionCart(session);
        validateCartAvailability(cart, false);

        OrderPricing pricing = calculatePricing(cart, deliveryInfoRequest);
        List<InvoiceLineResponse> invoiceItems = buildInvoiceLines(cart);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setStatus("pending");
        order = orderRepository.saveAndFlush(order);
        if (isBlank(order.getOrderId())) {
            throw new InvalidOrderException("Cannot create delivery because order ID was not generated.");
        }

        for (CartItem item : cart.getItems()) {
            Product product = findProduct(item.getProductId(), false);

            OrderItem orderItem = new OrderItem();
            orderItem.setId(new OrderItemId(order.getOrderId(), product.getProductId()));
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQOrder());
            orderItemRepository.save(orderItem);
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(order.getOrderId());
        delivery.setRecipientName(deliveryInfoRequest.getRecipientName().trim());
        delivery.setPhoneNumber(deliveryInfoRequest.getPhoneNumber().trim());
        delivery.setEmail(trimToNull(deliveryInfoRequest.getEmail()));
        delivery.setDeliveryProvince(deliveryInfoRequest.getDeliveryProvince().trim());
        delivery.setDetailAddress(deliveryInfoRequest.getDetailAddress().trim());
        delivery.setNote(trimToNull(deliveryInfoRequest.getNote()));
        deliveryRepository.saveAndFlush(delivery);

        Invoice invoice = new Invoice(order);
        invoice.setSubTotalExVAT(pricing.subtotalExVat());
        invoice.setSubTotalIncVAT(pricing.subtotalIncVat());
        invoice.setShippingFee(pricing.shippingFee());
        invoice = invoiceRepository.save(invoice);

        return toInvoiceResponse(invoice, invoiceItems);
    }

    @Override
    @Transactional
    public InvoiceResponse confirmPaidOrder(String orderId, HttpSession session) {
        if (isBlank(orderId)) {
            throw new InvalidOrderException("Order ID is required.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Order does not exist. ID: " + orderId));
        if (!"pending".equalsIgnoreCase(order.getStatus())) {
            throw new InvalidOrderException("Only pending orders can be confirmed as paid.");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);
        if (orderItems.isEmpty()) {
            throw new InvalidOrderException("Order has no items.");
        }

        List<InvoiceLineResponse> invoiceItems = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            Product product = findProduct(orderItem.getProduct().getProductId(), true);
            validateProductAvailability(product, orderItem.getQuantity());

            product.setQuantityInStock(product.getQuantityInStock() - orderItem.getQuantity());
            productRepository.save(product);
            invoiceItems.add(toInvoiceLine(product, orderItem.getQuantity()));
        }

        order.setStatus("pending");
        orderRepository.save(order);
        session.removeAttribute(CART_SESSION_KEY);

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvalidOrderException("Invoice does not exist for order ID: " + orderId));

        // Email notification and payment transaction persistence belong to the payment/mail modules.
        return toInvoiceResponse(invoice, invoiceItems);
    }

    private Cart resolveCart(PlaceOrderRequest placeOrderRequest, HttpSession session, boolean persistInSession) {
        if (placeOrderRequest != null && placeOrderRequest.getItems() != null && !placeOrderRequest.getItems().isEmpty()) {
            Cart cart = toCart(placeOrderRequest.getItems());
            if (persistInSession) {
                session.setAttribute(CART_SESSION_KEY, cart);
            }
            return cart;
        }
        return getSessionCart(session);
    }

    private Cart toCart(List<CartItemRequest> requestItems) {
        Cart cart = new Cart();
        for (CartItemRequest requestItem : requestItems) {
            if (requestItem == null || requestItem.getProductId() == null || requestItem.getQuantity() == null) {
                throw new InvalidOrderException("Cart item must include productId and quantity.");
            }
            cart.getItems().add(new CartItem(requestItem.getProductId(), requestItem.getQuantity()));
        }
        return cart;
    }

    private Cart getSessionCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CART_SESSION_KEY);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new InvalidOrderException("Your cart is empty.");
        }
        return cart;
    }

    private void validateCartAvailability(Cart cart, boolean lockProduct) {
        for (CartItem item : cart.getItems()) {
            if (item.getQOrder() <= 0) {
                throw new InvalidOrderException("Ordered quantity must be greater than zero.");
            }
            Product product = findProduct(item.getProductId(), lockProduct);
            validateProductAvailability(product, item.getQOrder());
        }
    }

    private Product findProduct(Integer productId, boolean lockProduct) {
        if (productId == null) {
            throw new InvalidOrderException("Product ID is required.");
        }
        return (lockProduct
                ? productRepository.findWithLockByProductId(productId)
                : productRepository.findById(productId))
                .orElseThrow(() -> new InvalidOrderException("Product does not exist. ID: " + productId));
    }

    private void validateProductAvailability(Product product, int requestedQuantity) {
        if (requestedQuantity <= 0) {
            throw new InvalidOrderException("Ordered quantity must be greater than zero.");
        }
        if (!"active".equalsIgnoreCase(product.getStatus())) {
            throw new InvalidOrderException("Product '" + product.getTitle() + "' is not available for sale.");
        }
        if (requestedQuantity > product.getQuantityInStock()) {
            throw new InvalidOrderException("Product '" + product.getTitle() + "' only has "
                    + product.getQuantityInStock() + " item(s) in stock, but " + requestedQuantity + " were ordered.");
        }
    }

    private void validateDeliveryInfoForShipping(DeliveryInfoRequest deliveryInfoRequest) {
        if (deliveryInfoRequest == null || isBlank(deliveryInfoRequest.getDeliveryProvince())) {
            throw new InvalidOrderException("Delivery province is required to calculate shipping fee.");
        }
    }

    private void validateDeliveryInfoForInvoice(DeliveryInfoRequest deliveryInfoRequest) {
        if (deliveryInfoRequest == null) {
            throw new InvalidOrderException("Delivery information is required.");
        }
        if (isBlank(deliveryInfoRequest.getRecipientName())
                || isBlank(deliveryInfoRequest.getPhoneNumber())
                || isBlank(deliveryInfoRequest.getDeliveryProvince())
                || isBlank(deliveryInfoRequest.getDetailAddress())) {
            throw new InvalidOrderException("Recipient name, phone number, province, and detailed address are required.");
        }
        if (!deliveryInfoRequest.getPhoneNumber().trim().matches("\\d{10}")) {
            throw new InvalidOrderException("Phone number must contain exactly 10 digits.");
        }
    }

    private OrderPricing calculatePricing(Cart cart, DeliveryInfoRequest deliveryInfoRequest) {
        long subtotalExVat = 0;
        double totalWeight = 0;

        for (CartItem item : cart.getItems()) {
            Product product = findProduct(item.getProductId(), false);
            subtotalExVat += product.getSellingPrice() * item.getQOrder();
            totalWeight += product.getWeight() * item.getQOrder();
        }

        long vat = calculateVat(subtotalExVat);
        long subtotalIncVat = subtotalExVat + vat;
        long shippingFee = calculateShippingFee(deliveryInfoRequest.getDeliveryProvince(), totalWeight, subtotalExVat);
        return new OrderPricing(subtotalExVat, vat, subtotalIncVat, shippingFee);
    }

    private List<InvoiceLineResponse> buildInvoiceLines(Cart cart) {
        List<InvoiceLineResponse> invoiceItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = findProduct(item.getProductId(), false);
            invoiceItems.add(toInvoiceLine(product, item.getQOrder()));
        }
        return invoiceItems;
    }

    private long calculateVat(long subtotalExVat) {
        return Math.round(subtotalExVat * VAT_RATE);
    }

    private long calculateShippingFee(String deliveryProvince, double totalWeight, long subtotalExVat) {
        long rawShippingFee = calculateRawShippingFee(deliveryProvince, totalWeight);
        if (subtotalExVat > FREE_SHIPPING_THRESHOLD) {
            return Math.max(rawShippingFee - Math.min(rawShippingFee, MAX_FREE_SHIPPING_DISCOUNT), 0);
        }
        return rawShippingFee;
    }

    private long calculateRawShippingFee(String deliveryProvince, double totalWeight) {
        String province = deliveryProvince.toLowerCase(Locale.ROOT);
        boolean innerCity = province.contains("ha noi")
                || province.contains("hanoi")
                || province.contains("ho chi minh");

        long fee = innerCity ? 22_000L : 30_000L;
        double baseWeight = innerCity ? 3.0 : 0.5;
        if (totalWeight > baseWeight) {
            fee += (long) Math.ceil((totalWeight - baseWeight) / 0.5) * 2_500L;
        }
        return fee;
    }

    private InvoiceLineResponse toInvoiceLine(Product product, int quantity) {
        return InvoiceLineResponse.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .category(product.getCategory())
                .image(product.getImage())
                .quantity(quantity)
                .unitPriceExVat(product.getSellingPrice())
                .amountExVat(product.getSellingPrice() * quantity)
                .build();
    }

    private InvoiceResponse toInvoiceResponse(Invoice invoice, List<InvoiceLineResponse> invoiceItems) {
        long subtotalExVat = invoice.getSubTotalExVAT();
        long vat = invoice.getSubTotalIncVAT() - subtotalExVat;
        long total = invoice.getSubTotalIncVAT() + invoice.getShippingFee();

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .orderId(invoice.getOrder().getOrderId())
                .issueDate(invoice.getIssueDate())
                .items(invoiceItems)
                .subtotalExVAT(subtotalExVat)
                .vat(vat)
                .subtotalIncVAT(invoice.getSubTotalIncVAT())
                .shippingFee(invoice.getShippingFee())
                .total(total)
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private record OrderPricing(long subtotalExVat, long vat, long subtotalIncVat, long shippingFee) {
    }
}

package com.aims.service.placeorder;

import com.aims.constants.OrderStatusValues;
import com.aims.dto.request.CreateInvoiceRequest;
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
import com.aims.service.shipping.ShippingFeeRequest;
import com.aims.service.shipping.ShippingFeeResult;
import com.aims.service.shipping.ShippingFeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InvoiceCreationService {
    private static final double VAT_RATE = 0.10;

    private final CheckoutCartService checkoutCartService;
    private final DeliveryValidationService deliveryValidationService;
    private final ShippingFeeService shippingFeeService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final InvoiceRepository invoiceRepository;

    public InvoiceCreationService(
            CheckoutCartService checkoutCartService,
            DeliveryValidationService deliveryValidationService,
            ShippingFeeService shippingFeeService,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            DeliveryRepository deliveryRepository,
            InvoiceRepository invoiceRepository) {
        this.checkoutCartService = checkoutCartService;
        this.deliveryValidationService = deliveryValidationService;
        this.shippingFeeService = shippingFeeService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.deliveryRepository = deliveryRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        if (request == null) {
            throw new InvalidOrderException("Invoice request is required.");
        }

        ValidatedDeliveryInfo deliveryInfo = deliveryValidationService.validate(request.getDeliveryInfo());
        CartValidationResult cart = checkoutCartService.validateAndBuildCartContext(request.getItems());

        long vat = Math.round(cart.subtotalExVat() * VAT_RATE);
        long subtotalIncVat = cart.subtotalExVat() + vat;
        ShippingFeeResult shippingFee = shippingFeeService.calculate(new ShippingFeeRequest(
                deliveryInfo.deliveryProvince(),
                cart.subtotalExVat(),
                cart.shippingItems()));

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setStatus(OrderStatusValues.AWAITING_PAYMENT);
        order = orderRepository.saveAndFlush(order);
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
            throw new InvalidOrderException("Cannot create delivery because order ID was not generated.");
        }

        for (int i = 0; i < cart.products().size(); i++) {
            Product product = cart.products().get(i);
            OrderItem orderItem = new OrderItem();
            orderItem.setId(new OrderItemId(order.getOrderId(), product.getProductId()));
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cart.quantities().get(i));
            orderItemRepository.save(orderItem);
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(order.getOrderId());
        delivery.setRecipientName(deliveryInfo.recipientName());
        delivery.setPhoneNumber(deliveryInfo.phoneNumber());
        delivery.setEmail(deliveryInfo.email());
        delivery.setDeliveryProvince(deliveryInfo.deliveryProvince());
        delivery.setDetailAddress(deliveryInfo.detailAddress());
        delivery.setNote(deliveryInfo.note());
        deliveryRepository.saveAndFlush(delivery);

        Invoice invoice = new Invoice(order);
        invoice.setSubTotalExVAT(cart.subtotalExVat());
        invoice.setSubTotalIncVAT(subtotalIncVat);
        invoice.setShippingFee(shippingFee.shippingFee());
        invoice = invoiceRepository.save(invoice);

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .orderId(invoice.getOrder().getOrderId())
                .issueDate(invoice.getIssueDate())
                .items(cart.invoiceItems())
                .subtotalExVAT(cart.subtotalExVat())
                .vat(vat)
                .subtotalIncVAT(subtotalIncVat)
                .shippingFee(shippingFee.shippingFee())
                .total(subtotalIncVat + shippingFee.shippingFee())
                .build();
    }
}

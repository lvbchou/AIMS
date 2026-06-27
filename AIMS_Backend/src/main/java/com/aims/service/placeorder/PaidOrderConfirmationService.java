package com.aims.service.placeorder;

import com.aims.constants.OrderStatusValues;
import com.aims.dto.order.InvoiceLineResponse;
import com.aims.dto.order.InvoiceResponse;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.entity.OrderItem;
import com.aims.entity.TransactionStatus;
import com.aims.entity.product.Product;
import com.aims.exception.InvalidOrderException;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderItemRepository;
import com.aims.repository.OrderRepository;
import com.aims.repository.PaymentTransactionRepository;
import com.aims.repository.product.ProductRepository;
import com.aims.service.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaidOrderConfirmationService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final NotificationService notificationService;

    public PaidOrderConfirmationService(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            InvoiceRepository invoiceRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            NotificationService notificationService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public InvoiceResponse confirmPaidOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new InvalidOrderException("Order ID is required.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Order does not exist. ID: " + orderId));
        // Accept either AWAITING_PAYMENT (payment not yet processed) or
        // PENDING_PROCESSING (payment callback already completed via VietQR webhook/test).
        // The real guard is the SUCCESS transaction check below — not the order status.
        if (!OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus())
                && !OrderStatusValues.PENDING_PROCESSING.equals(order.getStatus())) {
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
}

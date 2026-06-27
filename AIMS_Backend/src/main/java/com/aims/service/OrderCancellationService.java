package com.aims.service;

import com.aims.constants.OrderStatusValues;
import com.aims.dto.GatewayRefundResult;
import com.aims.dto.order.InvoiceLineItemDTO;
import com.aims.dto.OrderCancellationDetailsDTO;
import com.aims.dto.common.ApiResponse;
import com.aims.entity.*;
import com.aims.entity.product.Product;
import com.aims.exception.InvalidOrderException;
import com.aims.exception.InvoiceNotFoundException;
import com.aims.exception.OrderNotFoundException;
import com.aims.exception.PaymentException;
import com.aims.gateway.IRefundableGateway;
import com.aims.gateway.IPaymentGateway;
import com.aims.gateway.PaymentGatewayRegistry;
import com.aims.gateway.PaymentRefundParams;
import com.aims.repository.*;
import com.aims.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OrderCancellationService - coordinates order cancellation and refund logic.
 */
@Service
public class OrderCancellationService implements IOrderCancellationService {
    private static final Logger log = LoggerFactory.getLogger(OrderCancellationService.class);

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RefundTransactionRepository refundTransactionRepository;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final NotificationService notificationService;

    public OrderCancellationService(
            OrderRepository orderRepository,
            InvoiceRepository invoiceRepository,
            DeliveryRepository deliveryRepository,
            OrderItemRepository orderItemRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            RefundTransactionRepository refundTransactionRepository,
            PaymentGatewayRegistry gatewayRegistry,
            NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.deliveryRepository = deliveryRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.refundTransactionRepository = refundTransactionRepository;
        this.gatewayRegistry = gatewayRegistry;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderCancellationDetailsDTO getCancellationDetails(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new InvalidOrderException("Order ID is required.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotFoundException(orderId));

        Delivery delivery = deliveryRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Delivery information does not exist for order ID: " + orderId));

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

        // Get successful payment transaction if exists
        Optional<PaymentTransaction> txnOpt = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(invoice.getInvoiceId(), TransactionStatus.success);

        String paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "PAYPAL";
        String transactionId = "N/A";
        String transactionContent = "N/A";
        String transactionTimeDisplay = "N/A";

        if (txnOpt.isPresent()) {
            PaymentTransaction txn = txnOpt.get();
            transactionId = txn.getTransactionId();
            transactionContent = txn.getContent();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            ZonedDateTime zdt = txn.getTransactionTime().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
            transactionTimeDisplay = zdt.format(fmt);
        }

        boolean eligible = OrderStatusValues.PENDING_PROCESSING.equals(order.getStatus()) 
                || OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus());

        return OrderCancellationDetailsDTO.builder()
                .orderId(orderId)
                .orderStatus(order.getStatus())
                .eligibleForCancellation(eligible)
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
                .paymentMethod(paymentMethod)
                .transactionId(transactionId)
                .transactionContent(transactionContent)
                .transactionTimeDisplay(transactionTimeDisplay)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<String> cancelOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new InvalidOrderException("Order ID is required.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (OrderStatusValues.CANCELLED.equals(order.getStatus())) {
            return new ApiResponse<>(false, "Order is already cancelled.", "ALREADY_CANCELLED");
        }

        if (!OrderStatusValues.PENDING_PROCESSING.equals(order.getStatus()) 
                && !OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus())) {
            return new ApiResponse<>(false, "Order cannot be cancelled in its current state.", "NOT_ELIGIBLE");
        }

        PaymentMethod method = order.getPaymentMethod();
        if (method == null) {
            method = PaymentMethod.PAYPAL;
        }

        if (method == PaymentMethod.VIET_QR) {
            // Business rule: DO NOT call any refund API, DO NOT change Order status, DO NOT create refund transaction.
            return new ApiResponse<>(false, "You will be contacted by the Product Manager to process your refund manually.", "VIETQR_MANUAL_REFUND");
        }

        // PayPal flow
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotFoundException(orderId));

        PaymentTransaction transaction = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(invoice.getInvoiceId(), TransactionStatus.success)
                .orElseThrow(() -> new PaymentException("No successful payment transaction found for this order."));

        if (TransactionStatus.refunded.equals(transaction.getStatus())) {
            return new ApiResponse<>(false, "This transaction has already been refunded.", "ALREADY_REFUNDED");
        }

        // Get PayPal gateway
        IPaymentGateway gateway = gatewayRegistry.getGateway(PaymentMethod.PAYPAL);
        if (!(gateway instanceof IRefundableGateway)) {
            throw new UnsupportedOperationException("Refund is not supported by the PayPal gateway.");
        }
        IRefundableGateway refundableGateway = (IRefundableGateway) gateway;

        // Perform refund
        PaymentRefundParams refundParams = new PaymentRefundParams(
                transaction.getTransactionId(),
                invoice.getTotalAmount(),
                "USD"
        );

        GatewayRefundResult refundResult = refundableGateway.refundPayment(refundParams);

        if (refundResult.isSuccess()) {
            // Persist refund transaction
            RefundTransaction refundTx = new RefundTransaction(transaction);
            refundTx.setRefundTransactionId(refundResult.getRefundId());
            refundTransactionRepository.save(refundTx);

            // Update payment transaction status
            transaction.setStatus(TransactionStatus.refunded);
            paymentTransactionRepository.save(transaction);

            // Update order status
            order.setStatus(OrderStatusValues.CANCELLED);
            orderRepository.save(order);

            // Notify customer
            try {
                notificationService.sendOrderCancellationNotification(orderId);
            } catch (Exception ex) {
                log.warn("Failed to send order cancellation notification for orderId={}: {}", orderId, ex.getMessage());
            }

            return new ApiResponse<>(true, "Order cancelled and fully refunded successfully.", "SUCCESS");
        } else {
            throw new PaymentException("PayPal Refund API call failed: " + refundResult.getMessage());
        }
    }
}

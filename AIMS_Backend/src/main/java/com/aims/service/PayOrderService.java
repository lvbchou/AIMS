package com.aims.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.aims.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aims.constants.OrderStatusValues;
import com.aims.dto.InvoiceScreenDTO;
import com.aims.dto.OrderConfirmationDTO;
import com.aims.dto.VietQRCodeResponseDTO;
import com.aims.dto.VietQRCallbackRequestDTO;
import com.aims.repository.DeliveryRepository;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderItemRepository;
import com.aims.repository.OrderRepository;
import com.aims.repository.PaymentTransactionRepository;
import com.aims.subsystem.IPaymentQRCode;
import com.aims.subsystem.vietqr.VietQRController;
import com.aims.subsystem.vietqr.VietQRBoundary;
import com.aims.subsystem.vietqr.QRAccessTokenRequest;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Communicational Cohesion.
 *
 * This service operates on the same payment-related data set across invoice,
 * QR generation, callback processing, and confirmation flows.
 *
 * SOLID VIOLATION: Single Responsibility Principle (SRP)
 *
 * Problem: This class handles at least 7 distinct responsibilities that should be separated:
 *   1. Invoice data assembly for pre-payment display (getInvoiceScreen)
 *   2. QR code generation orchestration (requestVietQrDisplay)
 *   3. Payment transaction completion and order status update (completeVietQrPayment)
 *   4. Raw webhook payload processing (handleVietQrWebhook)
 *   5. DTO-mapped callback processing (handleVietQrPaymentCallback)
 *   6. Test/mock callback simulation (triggerVietQRTestCallback)
 *   7. Post-payment confirmation data assembly (getOrderConfirmation)
 * Impact: A change to invoice display logic could inadvertently affect callback processing.
 *   The class has low cohesion because it combines presentation-data assembly,
 *   payment processing, and order status management in one place.
 * Improvement:
 *   - Extract InvoiceQueryService for getInvoiceScreen and getOrderConfirmation
 *   - Extract VietQrPaymentProcessor for completeVietQrPayment, handleVietQrWebhook,
 *     handleVietQrPaymentCallback, and triggerVietQRTestCallback
 *   - Extract PaymentStatusQueryService for isPaymentSuccessful and getOrderPaymentStatus
 *   - Keep PayOrderService as a thin facade delegating to these focused services
 *
 * SOLID VIOLATION: Open/Closed Principle (OCP)
 *
 * Problem: The normalizePaymentStatus method uses if-else chains with hardcoded status
 *   values ("00", "SUCCESS", "FAILED", "99"). Adding a new payment gateway with different
 *   status codes (e.g. MoMo returning "0" for success) requires modifying this method.
 *   Similarly, triggerVietQRTestCallback is hardcoded to simulate only VietQR success;
 *   supporting a new payment provider would require adding new methods to this class.
 * Impact: Every new payment provider integration forces modification of this stable
 *   service class, increasing the risk of regression bugs in existing payment flows.
 * Improvement:
 *   - Define a PaymentStatusNormalizer interface with provider-specific implementations
 *     (e.g. VietQrStatusNormalizer, PayPalStatusNormalizer)
 *   - Use a Strategy Pattern so each payment method provides its own callback handler
 *   - Apply Factory Pattern to create the appropriate normalizer based on payment method
 *
 * SOLID: Liskov Substitution Principle (LSP) - Not Violated
 *
 * This class does not participate in an inheritance hierarchy. It is a concrete
 * service class with no subclasses, so LSP is not applicable in this context.
 *
 * SOLID: Interface Segregation Principle (ISP) - Not Violated
 *
 * This class does not implement any interface. It is a standalone service.
 * However, if an interface were extracted, it should be split into multiple
 * role-specific interfaces (e.g. InvoiceQueryable, PaymentProcessable,
 * PaymentStatusQueryable) rather than one large service interface, to avoid
 * forcing clients to depend on methods they do not use.
 *
 * SOLID VIOLATION: Dependency Inversion Principle (DIP)
 *
 * Problem: This high-level service directly depends on the concrete classes
 *   VietQRController and VietQRBoundary (both injected via constructor). While
 *   IPaymentQRCode is used as an abstraction for QR generation, the VietQRController
 *   and VietQRBoundary fields are concrete implementation references, creating tight
 *   coupling to the VietQR subsystem.
 * Impact: Replacing VietQR with a different QR payment provider (e.g. MoMo QR)
 *   would require modifying this service class. Unit testing is also harder because
 *   mocking concrete classes is less reliable than mocking interfaces.
 * Improvement:
 *   - Remove the direct dependency on VietQRController and VietQRBoundary
 *   - Define abstractions such as IPaymentCallbackHandler and IPaymentTokenProvider
 *   - Inject these abstractions via constructor so that different payment subsystems
 *     can be swapped without modifying this service
 *
 * @author Team 03
 * @since 1.0.0
 */
@Service
@Transactional
public class PayOrderService {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final IPaymentQRCode paymentQRCode;
    private final VietQRController vietQRController;
    private final VietQRBoundary vietQRBoundary;

    public PayOrderService(
            OrderRepository orderRepository,
            InvoiceRepository invoiceRepository,
            DeliveryRepository deliveryRepository,
            OrderItemRepository orderItemRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            IPaymentQRCode paymentQRCode,
            VietQRController vietQRController,
            VietQRBoundary vietQRBoundary) {
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.deliveryRepository = deliveryRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentQRCode = paymentQRCode;
        this.vietQRController = vietQRController;
        this.vietQRBoundary = vietQRBoundary;
    }

    /**
     * Returns the invoice screen data before payment.
     *
     * @param orderId the order identifier to look up.
     * @return invoice data to display to the user.
     * @throws com.aims.exception.OrderNotFoundException   if the order does not
     *                                                     exist.
     * @throws com.aims.exception.InvoiceNotFoundException if the invoice does not
     *                                                     exist.
     * @throws com.aims.exception.OrderNotPayableException if the order is not
     *                                                     payable.
     */
    @Transactional(readOnly = true)
    public InvoiceScreenDTO getInvoiceScreen(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.aims.exception.OrderNotFoundException(orderId));

        if (!OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus())) {
            throw new com.aims.exception.OrderNotPayableException("order not payable");
        }

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new com.aims.exception.InvoiceNotFoundException(orderId));

        deliveryRepository.findById(orderId)
                .orElseThrow(() -> new com.aims.exception.OrderNotPayableException("delivery missing"));

        long totalEx = invoice.getSubTotalExVAT();
        long totalInc = invoice.getSubTotalIncVAT();
        long deliveryFee = invoice.getShippingFee();
        long totalToPay = totalInc + deliveryFee;

        List<com.aims.entity.OrderItem> items = orderItemRepository.findAllWithProductByOrderId(orderId);
        java.util.List<com.aims.dto.InvoiceLineItemDTO> lines = new java.util.ArrayList<>();
        for (com.aims.entity.OrderItem oi : items) {
            com.aims.dto.InvoiceLineItemDTO line = com.aims.dto.InvoiceLineItemDTO.builder()
                    .productTitle(oi.getProduct().getTitle())
                    .quantity(oi.getQuantity())
                    .unitSellingPrice(oi.getProduct().getSellingPrice())
                    .lineTotalSellingPrice(oi.getProduct().getSellingPrice() * oi.getQuantity())
                    .build();
            lines.add(line);
        }

        InvoiceScreenDTO dto = InvoiceScreenDTO.builder()
                .orderId(orderId)
                .invoiceId(invoice.getInvoiceId())
                .lineItems(lines)
                .totalProductPriceExclVat(totalEx)
                .totalProductPriceInclVat(totalInc)
                .deliveryFee(deliveryFee)
                .totalAmountToBePaid(totalToPay)
                .build();
        return dto;
    }

    /**
     * Builds VietQR data for an order that is pending payment.
     *
     * @param orderId the order identifier used to create the QR.
     * @return QR data for the client to render.
     */
    public VietQRCodeResponseDTO requestVietQrDisplay(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.aims.exception.OrderNotFoundException(orderId));

        if (!OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus())) {
            throw new com.aims.exception.OrderNotPayableException("order not payable");
        }

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new com.aims.exception.InvoiceNotFoundException(orderId));

        deliveryRepository.findById(orderId)
                .orElseThrow(() -> new com.aims.exception.OrderNotPayableException("delivery missing"));

        // if already paid
        if (paymentTransactionRepository.existsByInvoiceIdAndStatus(invoice.getInvoiceId(),
                TransactionStatus.success)) {
            throw new com.aims.exception.PaymentAlreadyCompletedException(invoice.getInvoiceId());
        }

        // reuse existing pending
        java.util.Optional<com.aims.entity.PaymentTransaction> existing = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.pending);

        com.aims.entity.PaymentTransaction txnToReturn;
        if (existing.isPresent()) {
            txnToReturn = existing.get();
            if (txnToReturn.getTransactionId() == null || txnToReturn.getTransactionId().isBlank()) {
                txnToReturn.ensureTransactionId();
                txnToReturn = paymentTransactionRepository.save(txnToReturn);
            }
        } else {
            com.aims.entity.PaymentTransaction toSave = com.aims.entity.PaymentTransaction.pendingVietQr(invoice,
                    orderId);
            toSave.ensureTransactionId();
            txnToReturn = paymentTransactionRepository.save(toSave);
        }

        QRCode qrCode = paymentQRCode.getQRCode(order);

        VietQRCodeResponseDTO resp = VietQRCodeResponseDTO.builder()
                .orderId(orderId)
                .invoiceId(invoice.getInvoiceId())
                .transactionId(txnToReturn.getTransactionId())
                .qrCodeImageBase64(qrCode.getQrCode())
                .vietQrReference(qrCode.getQrLink())
                .totalAmountToBePaid(invoice.getSubTotalIncVAT() + invoice.getShippingFee())
                .content(qrCode.getContent())
                .build();

        return resp;
    }

    /**
     * Completes a VietQR transaction when the callback confirms success.
     *
     * @param orderId               related order identifier.
     * @param transactionTimeMillis transaction time as epoch millis, may be
     *                              {@code null}.
     * @param referenceNumber       reference number from VietQR.
     * @param callbackAmount        amount sent back in the callback.
     * @return the transaction ID that was confirmed successfully.
     */
    public String completeVietQrPayment(String orderId, Long transactionTimeMillis, String referenceNumber,
            long callbackAmount) {
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new com.aims.exception.InvoiceNotFoundException(orderId));

        long expectedAmount = invoice.getSubTotalIncVAT() + invoice.getShippingFee();
        if (callbackAmount != expectedAmount) {
            throw new IllegalArgumentException("callback amount does not match invoice total");
        }

        java.util.Optional<com.aims.entity.PaymentTransaction> successTxn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.success);
        if (successTxn.isPresent()) {
            return successTxn.get().getTransactionId();
        }

        com.aims.entity.PaymentTransaction txn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.pending)
                .orElseThrow(() -> new com.aims.exception.PaymentTransactionNotFoundException(
                        "no pending transaction for order " + orderId));

        txn.setStatus(TransactionStatus.success);
        txn.setTransactionTime(transactionTimeMillis == null ? LocalDateTime.now()
                : LocalDateTime.ofEpochSecond(transactionTimeMillis / 1000,
                        (int) ((transactionTimeMillis % 1000) * 1_000_000),
                        java.time.ZoneOffset.UTC));
        paymentTransactionRepository.save(txn);

        orderRepository.findById(orderId).ifPresent(o -> {
            o.setStatus(OrderStatusValues.PENDING_PROCESSING);
            orderRepository.save(o);
        });

        return txn.getTransactionId();
    }

    /**
     * Handles a raw webhook payload posted by VietQR.
     *
     * @param callbackData raw JSON callback payload.
     * @throws IllegalArgumentException if the callback cannot resolve a valid
     *                                  orderId.
     */
    public void handleVietQrWebhook(String callbackData) {
        PaymentResult result = paymentQRCode.checkPaymentStatus(callbackData);

        String orderId = resolveOrderId(result.getOrderId());
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("unable to resolve orderId from VietQR callback");
        }

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new com.aims.exception.InvoiceNotFoundException(orderId));

        PaymentTransaction txn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.pending)
                .orElseThrow(() -> new com.aims.exception.PaymentTransactionNotFoundException(
                        "no pending transaction for order " + orderId));

        VietQRCallbackRequestDTO dto = VietQRCallbackRequestDTO.builder()
                .transactionId(txn.getTransactionId())
                .paymentStatus(result.checkSuccess() ? "SUCCESS" : "FAILED")
                .content(result.getOrderId())
                .amount(0)
                .build();
        handleVietQrPaymentCallback(dto);
    }

    /**
     * Simulates a successful VietQR payment callback by directly completing the
     * payment transaction in the database — exactly what the real transaction-sync
     * callback would do. This avoids E222 sandbox errors from dev.vietqr.org.
     *
     * @param orderId order identifier.
     * @return result map with content, amount and transaction details.
     */
    @Transactional
    public java.util.Map<String, Object> triggerVietQRTestCallback(String orderId) {
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new com.aims.exception.InvoiceNotFoundException(orderId));

        long amount = invoice.getSubTotalIncVAT() + invoice.getShippingFee();

        // Build content the same way VietQRController does
        String safeOrderId = orderId.replace("-", "");
        if (safeOrderId.length() > 13) {
            safeOrderId = safeOrderId.substring(safeOrderId.length() - 13);
        }
        String content = "Order " + safeOrderId;

        // Directly complete the payment — same logic transaction-sync would trigger
        String transactionId = completeVietQrPayment(orderId, System.currentTimeMillis(),
                "MOCK-" + System.currentTimeMillis(), amount);

        System.out.println("[VietQR MockCallback] completed transactionId=" + transactionId
                + " for orderId=" + orderId + " content=" + content + " amount=" + amount);

        return java.util.Map.of(
                "transactionId", transactionId,
                "content", content,
                "amount", amount,
                "status", "SUCCESS");
    }

    /**
     * Processes a VietQR callback that has already been mapped to a DTO.
     *
     * @param dto callback data mapped to a DTO.
     * @throws IllegalArgumentException if the DTO or transactionId is invalid.
     */
    public void handleVietQrPaymentCallback(VietQRCallbackRequestDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("dto is required");

        String txnId = dto.getTransactionId();
        if (txnId == null)
            throw new IllegalArgumentException("transactionId");
        if (txnId.trim().isEmpty())
            throw new IllegalArgumentException("transactionId");

        String normalized = normalizePaymentStatus(dto.getPaymentStatus());

        com.aims.entity.PaymentTransaction txn = paymentTransactionRepository.findById(txnId)
                .orElseThrow(() -> new com.aims.exception.PaymentTransactionNotFoundException(txnId));

        // idempotent if already success
        if (TransactionStatus.success.equals(txn.getStatus())) {
            return;
        }

        if ("SUCCESS".equals(normalized)) {
            txn.setStatus(TransactionStatus.success);
            txn.setTransactionTime(LocalDateTime.now());
            paymentTransactionRepository.save(txn);

            // update order status
            java.util.Optional<com.aims.entity.Invoice> invOpt = invoiceRepository
                    .findById(txn.getInvoice().getInvoiceId());
            if (invOpt.isPresent()) {
                String orderId = invOpt.get().getOrder().getOrderId();
                orderRepository.findById(orderId).ifPresent(o -> {
                    o.setStatus(com.aims.constants.OrderStatusValues.PENDING_PROCESSING);
                    orderRepository.save(o);
                });
            }
        } else {
            // FAILED
            txn.setStatus(TransactionStatus.failed);
            paymentTransactionRepository.save(txn);
        }
    }

    /**
     * Checks whether a transaction is successful.
     *
     * @param transactionId transaction identifier to check.
     * @return {@code true} if the transaction is successful; otherwise
     *         {@code false}.
     */
    @Transactional(readOnly = true)
    public boolean isPaymentSuccessful(String transactionId) {
        return paymentTransactionRepository.findById(transactionId)
                .map(t -> com.aims.constants.PaymentTransactionStatusValues.SUCCESS
                        .equals(t.getStatus() != null ? t.getStatus().name() : null))
                .orElse(false);
    }

    /**
     * Returns payment status for an order by checking its latest transaction.
     * Used by the frontend to poll payment completion by orderId.
     *
     * @param orderId order identifier.
     * @return map with success flag, transactionId, and status string.
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getOrderPaymentStatus(String orderId) {
        java.util.Optional<Invoice> invoiceOpt = invoiceRepository.findByOrderOrderId(orderId);
        if (invoiceOpt.isEmpty()) {
            return java.util.Map.of("success", false, "status", "PENDING", "transactionId", "");
        }
        Invoice invoice = invoiceOpt.get();
        java.util.Optional<PaymentTransaction> successTxn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.success);
        if (successTxn.isPresent()) {
            return java.util.Map.of(
                    "success", true,
                    "status", "COMPLETED",
                    "transactionId", successTxn.get().getTransactionId());
        }
        return java.util.Map.of("success", false, "status", "PENDING", "transactionId", "");
    }

    /**
     * Returns the post-payment confirmation data for an order.
     *
     * @param orderId order identifier to confirm.
     * @return confirmation data to show the user.
     */
    @Transactional(readOnly = true)
    public OrderConfirmationDTO getOrderConfirmation(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.aims.exception.OrderNotFoundException(orderId));

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new com.aims.exception.InvoiceNotFoundException(orderId));

        Delivery delivery = deliveryRepository.findById(orderId)
                .orElseThrow(() -> new com.aims.exception.OrderNotPayableException("delivery missing"));

        // Return 404 if payment has not been confirmed yet — frontend can retry
        // gracefully
        com.aims.entity.PaymentTransaction txn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.success)
                .orElseThrow(() -> new com.aims.exception.PaymentTransactionNotFoundException(
                        "payment not yet confirmed for order " + orderId));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ZonedDateTime zdt = txn.getTransactionTime()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        String dt = zdt.format(fmt);

        // Build human-readable order name from product titles
        List<com.aims.entity.OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
        String orderName;
        if (items.isEmpty()) {
            orderName = "Đơn hàng #" + orderId.replace("-", "")
                    .substring(Math.max(0, orderId.replace("-", "").length() - 8)).toUpperCase();
        } else {
            String firstName = items.get(0).getProduct().getTitle();
            orderName = items.size() > 1
                    ? firstName + " và " + (items.size() - 1) + " sản phẩm khác"
                    : firstName;
        }

        OrderConfirmationDTO dto = OrderConfirmationDTO.builder()
                .customerName(delivery.getRecipientName())
                .phoneNumber(delivery.getPhoneNumber())
                .shippingAddress(delivery.getDetailAddress())
                .province(delivery.getDeliveryProvince())
                .totalAmountToBePaid(invoice.getSubTotalIncVAT() + invoice.getShippingFee())
                .transactionId(txn.getTransactionId())
                .transactionContent(txn.getContent())
                .orderName(orderName)
                .transactionDatetimeDisplay(dt)
                .build();

        return dto;
    }

    /**
     * Normalizes the payment status to the internal representation.
     *
     * @param status status value from the callback.
     * @return {@code SUCCESS} or {@code FAILED}.
     */
    private static String normalizePaymentStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("paymentStatus");
        }
        String trimmed = status.trim();
        if ("00".equals(trimmed) || "SUCCESS".equalsIgnoreCase(trimmed)) {
            return "SUCCESS";
        }
        if ("FAILED".equalsIgnoreCase(trimmed) || "99".equals(trimmed)) {
            return "FAILED";
        }
        throw new IllegalArgumentException("paymentStatus must be SUCCESS or FAILED");
    }

    /**
     * Resolves an orderId from the raw callback content.
     *
     * @param rawContent transfer content or callback content.
     * @return the normalized orderId.
     */
    private static String resolveOrderId(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return null;
        }
        String trimmed = rawContent.trim();
        // Backward compat: old format was "Order #ORD-001"
        if (trimmed.startsWith("Order #")) {
            return trimmed.substring("Order #".length()).trim();
        }
        // New format: content IS the orderId (e.g. "ORD-001")
        return trimmed;
    }
}

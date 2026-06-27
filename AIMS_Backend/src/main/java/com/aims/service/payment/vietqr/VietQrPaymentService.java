package com.aims.service.payment.vietqr;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aims.constants.OrderStatusValues;
import com.aims.dto.payment.VietQRCallbackRequestDTO;
import com.aims.dto.payment.VietQRCodeResponseDTO;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.PaymentResult;
import com.aims.entity.QRCode;
import com.aims.entity.TransactionStatus;
import com.aims.exception.InvoiceNotFoundException;
import com.aims.exception.OrderNotFoundException;
import com.aims.exception.OrderNotPayableException;
import com.aims.exception.PaymentAlreadyCompletedException;
import com.aims.exception.PaymentTransactionNotFoundException;
import com.aims.repository.DeliveryRepository;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderRepository;
import com.aims.repository.PaymentTransactionRepository;
import com.aims.subsystem.IPaymentQRCode;

import lombok.RequiredArgsConstructor;

/**
 * VietQrPaymentService — handles all VietQR payment lifecycle operations.
 *
 * Extracted from PayOrderService as part of the SRP refactoring.
 * This class is exclusively responsible for:
 *   1. Generating the VietQR code for display.
 *   2. Completing a VietQR payment when a callback confirms success.
 *   3. Processing raw webhook payloads posted by VietQR.
 *   4. Processing structured DTO callbacks (mapped from webhook or test trigger).
 *   5. Simulating a successful callback in the development/test environment.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VietQrPaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final IPaymentQRCode paymentQRCode;


    /**
     * Builds the VietQR code payload for an order that is awaiting payment.
     *
     * Validates order status, checks for duplicate payment, reuses any existing
     * pending transaction (idempotent), then delegates QR generation to the
     * IPaymentQRCode subsystem abstraction.
     *
     * @param orderId the order identifier to generate the QR for.
     * @return the QR code response DTO ready to send to the frontend.
     * @throws OrderNotFoundException            if the order does not exist.
     * @throws OrderNotPayableException          if the order is not AWAITING_PAYMENT or delivery missing.
     * @throws InvoiceNotFoundException          if no invoice exists for the order.
     * @throws PaymentAlreadyCompletedException  if the order has already been paid.
     */
    public VietQRCodeResponseDTO requestVietQrDisplay(String orderId) {
        // 1. Fetch the order and validate it is still awaiting payment
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!com.aims.constants.OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus())) {
            throw new OrderNotPayableException("Order " + orderId + " is not in AWAITING_PAYMENT state");
        }

        // 2. Fetch the invoice (contains amount to pay)
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotFoundException(orderId));

        // 3. Ensure delivery information exists (required to build QR content)
        deliveryRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotPayableException("Delivery information missing for order " + orderId));

        // 4. Guard against double-payment — throw if already paid
        if (paymentTransactionRepository.existsByInvoiceIdAndStatus(
                invoice.getInvoiceId(), TransactionStatus.success)) {
            throw new PaymentAlreadyCompletedException(invoice.getInvoiceId());
        }

        // 5. Reuse existing pending transaction if one already exists (idempotent QR display)
        //    This handles the case where the user refreshes the QR page.
        PaymentTransaction txnToReturn;
        Optional<PaymentTransaction> existing = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.pending);
        if (existing.isPresent()) {
            txnToReturn = existing.get();
            // Ensure the transaction has an ID (edge case: saved without one)
            if (txnToReturn.getTransactionId() == null || txnToReturn.getTransactionId().isBlank()) {
                txnToReturn.ensureTransactionId();
                txnToReturn = paymentTransactionRepository.save(txnToReturn);
            }
        } else {
            // 5b. Create a new pending transaction for this QR session
            PaymentTransaction toSave = PaymentTransaction.pendingVietQr(invoice, orderId);
            toSave.ensureTransactionId();
            txnToReturn = paymentTransactionRepository.save(toSave);
        }

        // 6. Generate the QR code via the payment subsystem abstraction (IPaymentQRCode)
        QRCode qrCode = paymentQRCode.getQRCode(order);

        // 7. Build and return the response DTO
        return VietQRCodeResponseDTO.builder()
                .orderId(orderId)
                .invoiceId(invoice.getInvoiceId())
                .transactionId(txnToReturn.getTransactionId())
                .qrCodeImageBase64(qrCode.getQrCode())
                .vietQrReference(qrCode.getQrLink())
                .totalAmountToBePaid(invoice.getSubTotalIncVAT() + invoice.getShippingFee())
                .content(qrCode.getContent())
                .build();
    }

    /**
     * Completes a VietQR payment transaction when the callback confirms success.
     *
     * <p>Idempotent: if a success transaction already exists for the invoice,
     * the existing transactionId is returned without re-processing.</p>
     *
     * @param orderId               the related order identifier.
     * @param transactionTimeMillis epoch-milliseconds of the transaction; uses now() if null.
     * @param referenceNumber       reference number from VietQR (stored for audit trail).
     * @param callbackAmount        the amount confirmed in the callback (must match invoice total).
     * @return the transactionId that was confirmed successfully.
     */
    public String completeVietQrPayment(String orderId, Long transactionTimeMillis,
            String referenceNumber, long callbackAmount) {

        // 1. Fetch the invoice to get the expected payment amount
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotFoundException(orderId));

        // 2. Idempotency check — if a success transaction already exists, return it immediately.
        //    This must happen BEFORE amount validation so that stale VietQR retries on already-paid
        //    orders do not cause a false amount-mismatch error.
        Optional<PaymentTransaction> successTxn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.success);
        if (successTxn.isPresent()) {
            return successTxn.get().getTransactionId();
        }

        // 3. Validate that the callback amount matches the invoice total (fraud guard).
        //    Only runs when the payment has NOT yet been confirmed.
        long expectedAmount = invoice.getSubTotalIncVAT() + invoice.getShippingFee();
        if (callbackAmount != expectedAmount) {
            throw new IllegalArgumentException(
                    "Callback amount " + callbackAmount + " does not match invoice total " + expectedAmount);
        }

        // 4. Fetch the pending transaction that was created when the QR was displayed.
        //    If none exists (e.g. test-callback triggered before QR was shown), create one
        //    automatically so the flow can complete without error.
        PaymentTransaction txn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.pending)
                .orElseGet(() -> {
                    PaymentTransaction newTxn = new PaymentTransaction(
                            null,
                            expectedAmount,
                            com.aims.entity.PaymentMethod.VIET_QR,
                            "Auto-created by test-callback");
                    newTxn.setInvoice(invoice);
                    return paymentTransactionRepository.save(newTxn);
                });

        // 5. Mark the transaction as successful and record the transaction time
        txn.setStatus(TransactionStatus.success);
        txn.setTransactionTime(transactionTimeMillis == null
                ? LocalDateTime.now()
                : LocalDateTime.ofEpochSecond(
                        transactionTimeMillis / 1000,
                        (int) ((transactionTimeMillis % 1000) * 1_000_000),
                        ZoneOffset.UTC));
        paymentTransactionRepository.save(txn);

        // 6. Advance the order status from AWAITING_PAYMENT → PENDING_PROCESSING (approved)
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatusValues.PENDING_PROCESSING);
            orderRepository.save(order);
        });

        return txn.getTransactionId();
    }

    /**
     * Handles a raw JSON webhook payload posted directly by VietQR.
     *
     * <p>Parses the payload using the payment subsystem, resolves the orderId
     * from the transfer content, then delegates to {@link #handleVietQrPaymentCallback}.</p>
     *
     * @param callbackData raw JSON callback string sent by VietQR's webhook.
     * @throws IllegalArgumentException if the orderId cannot be resolved from the payload.
     */
    public void handleVietQrWebhook(String callbackData) {
        // 1. Parse the raw callback payload via the payment subsystem abstraction
        PaymentResult result = paymentQRCode.checkPaymentStatus(callbackData);

        // 2. Resolve the orderId from the transfer content field
        String orderId = resolveOrderId(result.getOrderId());
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Unable to resolve orderId from VietQR webhook payload");
        }

        // 3. Find the invoice to locate the pending transaction
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotFoundException(orderId));

        // 4. Find the pending transaction for this invoice
        PaymentTransaction txn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.pending)
                .orElseThrow(() -> new PaymentTransactionNotFoundException(
                        "No pending transaction found for order: " + orderId));

        // 5. Map the raw result into a structured DTO and delegate to the DTO handler
        VietQRCallbackRequestDTO dto = VietQRCallbackRequestDTO.builder()
                .transactionId(txn.getTransactionId())
                .paymentStatus(result.checkSuccess() ? "SUCCESS" : "FAILED")
                .content(result.getOrderId())
                .amount(0)
                .build();

        handleVietQrPaymentCallback(dto);
    }

    /**
     * Processes a structured VietQR callback DTO (mapped from either a real webhook
     * or an internal test trigger).
     *
     * <p>Idempotent: if the transaction is already in SUCCESS state, returns immediately.</p>
     *
     * @param dto the callback data mapped to {@link VietQRCallbackRequestDTO}.
     * @throws IllegalArgumentException           if dto or transactionId is null/blank,
     *                                            or if paymentStatus is unrecognized.
     * @throws PaymentTransactionNotFoundException if no transaction matches the transactionId.
     */
    public void handleVietQrPaymentCallback(VietQRCallbackRequestDTO dto) {
        // 1. Validate the incoming DTO
        if (dto == null) {
            throw new IllegalArgumentException("Callback DTO must not be null");
        }
        String txnId = dto.getTransactionId();
        if (txnId == null || txnId.trim().isEmpty()) {
            throw new IllegalArgumentException("Callback DTO must contain a valid transactionId");
        }

        // 2. Normalize the raw status string to canonical SUCCESS or FAILED
        String normalized = normalizePaymentStatus(dto.getPaymentStatus());

        // 3. Fetch the transaction record from the database
        PaymentTransaction txn = paymentTransactionRepository.findById(txnId)
                .orElseThrow(() -> new PaymentTransactionNotFoundException(txnId));

        // 4. Idempotency check — do nothing if already successfully completed
        if (TransactionStatus.success.equals(txn.getStatus())) {
            return;
        }

        if ("SUCCESS".equals(normalized)) {
            // 5a. Mark transaction as successful and record the timestamp
            txn.setStatus(TransactionStatus.success);
            txn.setTransactionTime(LocalDateTime.now());
            paymentTransactionRepository.save(txn);

            // 5b. Advance the order status to PENDING_PROCESSING (approved)
            invoiceRepository.findById(txn.getInvoice().getInvoiceId())
                    .ifPresent(inv -> orderRepository.findById(inv.getOrder().getOrderId())
                            .ifPresent(order -> {
                                order.setStatus(OrderStatusValues.PENDING_PROCESSING);
                                orderRepository.save(order);
                            }));
        } else {
            // 5c. Mark transaction as failed
            txn.setStatus(TransactionStatus.failed);
            paymentTransactionRepository.save(txn);
        }
    }

    // ─────────────────────────── private helpers ────────────────────────────

    /**
     * Normalizes the raw payment status string from VietQR into an internal
     * canonical value ("SUCCESS" or "FAILED").
     *
     * Supported inputs:
     *   "00" or "SUCCESS" (case-insensitive) → "SUCCESS"
     *   "99" or "FAILED"  (case-insensitive) → "FAILED"
     *
     * @param status raw status value from the callback.
     * @return "SUCCESS" or "FAILED".
     * @throws IllegalArgumentException if the status is null or unrecognized.
     */
    private static String normalizePaymentStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("paymentStatus must not be null");
        }
        String trimmed = status.trim();
        if ("00".equals(trimmed) || "SUCCESS".equalsIgnoreCase(trimmed)) {
            return "SUCCESS";
        }
        if ("FAILED".equalsIgnoreCase(trimmed) || "99".equals(trimmed)) {
            return "FAILED";
        }
        throw new IllegalArgumentException(
                "Unrecognized paymentStatus: '" + status + "'. Must be SUCCESS or FAILED");
    }

    /**
     * Resolves an orderId from the raw transfer content sent by VietQR.
     *
     * Supports two formats:
     *   Legacy : "Order #ORD-001" → "ORD-001"
     *   Current: content IS the orderId directly (e.g. "ORD-001")
     *
     * @param rawContent the transfer content string from the VietQR callback.
     * @return the extracted orderId, or null if rawContent is blank.
     */
    private static String resolveOrderId(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return null;
        }
        String trimmed = rawContent.trim();
        // Legacy format: "Order #<orderId>"
        if (trimmed.startsWith("Order #")) {
            return trimmed.substring("Order #".length()).trim();
        }
        // Current format: content is the orderId itself
        return trimmed;
    }
}

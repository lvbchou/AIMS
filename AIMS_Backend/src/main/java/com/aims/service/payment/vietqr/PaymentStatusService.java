package com.aims.service.payment.vietqr;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aims.constants.PaymentTransactionStatusValues;
import com.aims.entity.Invoice;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.TransactionStatus;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.PaymentTransactionRepository;

import lombok.RequiredArgsConstructor;

/**
 * PaymentStatusService — read-only query service for payment status.
 *
 * Extracted from PayOrderService as part of the SRP refactoring.
 * This class is exclusively responsible for answering two questions:
 *   1. "Has a specific transaction been paid successfully?" (by transactionId)
 *   2. "What is the current payment status of this order?" (by orderId — for frontend polling)
 *
 * All methods are read-only (@Transactional(readOnly = true)) — this class
 * never modifies any database state.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentStatusService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Checks whether a specific payment transaction has been marked as successful.
     *
     * Used internally by other services to verify payment completion
     * before performing post-payment operations.
     *
     * @param transactionId the transaction identifier to check.
     * @return {@code true} if the transaction exists and its status is SUCCESS;
     *         {@code false} if the transaction does not exist or has any other status.
     */
    public boolean isPaymentSuccessful(String transactionId) {
        // 1. Attempt to fetch the transaction by its ID.
        //    If not found, safely return false (no exception needed — caller just needs a boolean).
        return paymentTransactionRepository.findById(transactionId)
                // 2. Map the found transaction to a boolean:
                //    compare its status (as a String via .name()) against the SUCCESS constant.
                //    Guard against null status to avoid NullPointerException.
                .map(txn -> PaymentTransactionStatusValues.SUCCESS
                        .equals(txn.getStatus() != null ? txn.getStatus().name() : null))
                // 3. If the transaction was not found, orElse(false) returns false gracefully.
                .orElse(false);
    }

    /**
     * Returns the current payment status of an order, designed for frontend polling.
     *
     * The frontend calls this endpoint repeatedly (polling) after displaying the QR code,
     * waiting for VietQR to confirm the payment via webhook.
     *
     * Response map always contains three keys:
     *   - "success"       : boolean — whether the order has been paid
     *   - "status"        : String  — "COMPLETED" if paid, "PENDING" otherwise
     *   - "transactionId" : String  — the successful transaction ID, or empty string
     *
     * @param orderId the order identifier to check payment status for.
     * @return an immutable map containing the three keys described above.
     */
    public Map<String, Object> getOrderPaymentStatus(String orderId) {
        // 1. Try to find the invoice associated with this order.
        //    An invoice is only created AFTER the user submits delivery info (createInvoice).
        //    If no invoice exists, the order is definitely not paid yet.
        Optional<Invoice> invoiceOpt = invoiceRepository.findByOrderOrderId(orderId);
        if (invoiceOpt.isEmpty()) {
            // Return PENDING early — no invoice means no payment has been attempted.
            return Map.of("success", false, "status", "PENDING", "transactionId", "");
        }

        Invoice invoice = invoiceOpt.get();

        // 2. Check if a SUCCESS transaction already exists for this invoice.
        //    The query orders by transactionTime DESC and takes the first result,
        //    so if multiple success records exist (edge case), the latest is used.
        Optional<PaymentTransaction> successTxn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.success);

        if (successTxn.isPresent()) {
            // 3a. Payment confirmed — return COMPLETED with the transactionId for the frontend
            //     to use in the confirmation page navigation.
            return Map.of(
                    "success", true,
                    "status", "COMPLETED",
                    "transactionId", successTxn.get().getTransactionId());
        }

        // 3b. Invoice exists but no success transaction yet — payment still pending.
        //     Frontend will retry polling on the next interval.
        return Map.of("success", false, "status", "PENDING", "transactionId", "");
    }
}

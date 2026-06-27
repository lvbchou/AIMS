package com.aims.dto.vietqr;

import java.util.Map;

/**
 * Response DTO for VietQR transaction-sync callbacks.
 *
 * VietQR expects a specific JSON structure containing an {@code error} flag,
 * an optional {@code errorReason}, a {@code toastMessage}, and an {@code object}
 * map carrying the reference transaction ID on success.
 *
 * Moved from inner record inside {@code VietQRCallbackEndpoint} to a standalone
 * class so that any class needing this DTO does not depend on the controller.
 *
 * @author Team 03
 * @since 1.0.0
 */
public record TransactionSyncResponse(
        boolean error,
        String errorReason,
        String toastMessage,
        Map<String, String> object) {

    /**
     * Creates a successful transaction-sync response.
     *
     * @param refTransactionId reference transaction ID to return to VietQR.
     * @return a success response.
     */
    public static TransactionSyncResponse success(String refTransactionId) {
        return new TransactionSyncResponse(
                false, null,
                "Transaction processed successfully",
                Map.of("reftransactionid", refTransactionId));
    }

    /**
     * Creates an error response for transaction-sync.
     *
     * @param errorReason internal error code.
     * @param message     human-readable message returned to VietQR.
     * @return an error response.
     */
    public static TransactionSyncResponse error(String errorReason, String message) {
        return new TransactionSyncResponse(true, errorReason, message, null);
    }
}

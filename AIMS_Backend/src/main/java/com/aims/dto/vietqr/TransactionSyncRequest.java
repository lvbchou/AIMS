package com.aims.dto.vietqr;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for synchronous VietQR transaction callbacks.
 *
 * Moved from inner record inside {@code VietQRCallbackEndpoint} to
 * a standalone top-level class. Any test or service that needs this
 * DTO no longer has to import the entire controller.
 *
 * @author Team 03
 * @since 1.0.0
 */
public record TransactionSyncRequest(
        @JsonProperty("bankaccount") String bankAccount,
        Long amount,
        String transType,
        String content,
        @JsonProperty("transactionid") String transactionId,
        @JsonProperty("transactiontime") Long transactionTime,
        @JsonProperty("referencenumber") String referenceNumber,
        @JsonProperty("orderId") String orderId,
        String terminalCode,
        String subTerminalCode,
        String serviceCode,
        String urlLink,
        String sign,
        @JsonProperty("bankCode") String bankCode) {
}

package com.aims.dto.vietqr;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for test/simulated VietQR callbacks.
 *
 * Moved from inner record inside {@code VietQRCallbackEndpoint}.
 *
 * @author Team 03
 * @since 1.0.0
 */
public record TestCallbackRequest(
        @JsonProperty("bankAccount") String bankAccount,
        String content,
        long amount,
        String transType,
        String bankCode) {
}

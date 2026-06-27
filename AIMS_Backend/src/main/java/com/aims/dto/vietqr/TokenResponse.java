package com.aims.dto.vietqr;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for the VietQR token generation endpoint.
 *
 * Moved from inner record inside {@code VietQRCallbackEndpoint} to
 * a standalone top-level class to satisfy ISP — consumers of this DTO
 * no longer need to depend on the full controller class.
 *
 * @author Team 03
 * @since 1.0.0
 */
public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") int expiresIn) {
}

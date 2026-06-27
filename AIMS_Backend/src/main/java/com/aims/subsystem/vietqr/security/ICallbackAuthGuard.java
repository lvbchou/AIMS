package com.aims.subsystem.vietqr.security;

/**
 * Interface defining authentication validation for inbound webhook and synchronization calls from VietQR.
 */
public interface ICallbackAuthGuard {

    /**
     * Validates an Authorization header from an inbound VietQR request.
     * Supports both Basic (for token issuance) and Bearer (for callbacks) schemes.
     *
     * @param authorizationHeader the raw Authorization header value.
     * @return {@code true} if the header is valid; {@code false} otherwise.
     */
    boolean validate(String authorizationHeader);

    /**
     * Returns the partner token secret used to issue Bearer tokens to VietQR.
     *
     * @return the configured token secret string.
     */
    String getTokenSecret();
}

package com.aims.subsystem.vietqr.security;

/**
 * Interface defining credential provider for outbound VietQR integration calls.
 */
public interface IApiCredentialProvider {

    /**
     * Builds a Basic Authorization header using the configured partner credentials.
     * Used by VietQRBoundary when requesting an access token from the VietQR API.
     *
     * @return a {@code "Basic <base64>"} header string ready for use in HTTP requests.
     */
    String buildBasicAuthHeader();
}

package com.aims.subsystem.vietqr;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This request object holds only the credentials needed to acquire a VietQR
 * access token.
 *
 * @author Team 03
 * @since 1.0.0
 */
public class QRAccessTokenRequest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String merchantId;
    private String apiKey;

    public QRAccessTokenRequest() {
    }

    public QRAccessTokenRequest(String merchantId, String apiKey) {
        this.merchantId = merchantId;
        this.apiKey = apiKey;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Builds a {@code Basic} header from the merchantId and apiKey.
     *
     * @return the Authorization header value.
     */
    public String buildAuthorizationHeader() {
        String credentials = merchantId + ":" + apiKey;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }

    /**
     * Serializes the request to JSON for debugging or transport.
     *
     * @return the request JSON string.
     */
    public String buildRequestBody() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing VietQR token request", e);
        }
    }
}
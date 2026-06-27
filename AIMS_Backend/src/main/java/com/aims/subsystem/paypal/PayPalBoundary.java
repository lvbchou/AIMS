// Coupling Level: Stamp Coupling, Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: It is stamp-coupled to the custom DTO CreateOrderRequest in createOrder since it
//   receives the custom request object as a parameter. It is data-coupled via simple strings for access
//   tokens and order IDs.
// Reason for Cohesion: The class acts exclusively as the HTTP network client for the external PayPal API,
//   with all of its methods serving to make outbound API requests (OAuth, create order, capture order).
/**
 * SOLID Principles Analysis (refactored — R3):
 * - **DIP Compliance**: Previously instantiated {@link java.net.http.HttpClient} directly in the
 *   constructor, making it impossible to inject a mock or stub in unit tests. The client is now
 *   received via constructor injection, declared as a Spring bean in
 *   {@link com.aims.subsystem.paypal.config.PayPalConfig}.
 * - **SRP Compliance**: Serialization of {@link CreateOrderRequest} has been moved to
 *   {@link PayPalResponseMapper#serializeCreateOrder} (R4), keeping this class focused purely
 *   on HTTP I/O.
 */
package com.aims.subsystem.paypal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * PayPalBoundary — the raw HTTP client for the PayPal REST API.
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Fetch OAuth2 access tokens via {@code POST /v1/oauth2/token}.</li>
 *   <li>Create PayPal orders via {@code POST /v2/checkout/orders}.</li>
 *   <li>Capture PayPal orders via {@code POST /v2/checkout/orders/{id}/capture}.</li>
 * </ul>
 *
 * <p>This class performs only HTTP I/O. All serialization is delegated to
 * {@link PayPalResponseMapper} and all token caching is delegated to
 * {@link PayPalAuthManager}.</p>
 */
public class PayPalBoundary {

    private final String baseUrl;
    private final HttpClient httpClient;

    /**
     * Constructs the boundary with an injected HTTP client.
     *
     * <p><strong>DIP compliance (R3):</strong> The {@link HttpClient} is received via
     * constructor injection declared in
     * {@link com.aims.subsystem.paypal.config.PayPalConfig#paypalHttpClient()}. This
     * removes the previous direct instantiation ({@code HttpClient.newHttpClient()})
     * and makes the boundary testable with a mock or WireMock stub.</p>
     *
     * @param baseUrl    the PayPal REST API base URL (e.g. {@code https://api-m.sandbox.paypal.com}).
     * @param httpClient the shared HTTP client to use for all outbound calls.
     */
    public PayPalBoundary(String baseUrl, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
    }

    /**
     * Fetches a PayPal OAuth2 access token.
     *
     * @param authHeader the {@code Basic <base64>} Authorization header value.
     * @return the raw JSON response body from PayPal.
     */
    String getAccessToken(String authHeader) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/oauth2/token"))
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get PayPal access token", e);
        }
    }

    /**
     * Creates a PayPal order.
     *
     * <p>Serialization of {@code requestPayload} is delegated to
     * {@link PayPalResponseMapper#serializeCreateOrder} (R4), which reuses
     * the shared {@link com.fasterxml.jackson.databind.ObjectMapper} rather
     * than creating a new one per call.</p>
     *
     * @param accessToken    a valid OAuth2 bearer token.
     * @param requestPayload the order parameters DTO.
     * @return the raw JSON response body from PayPal.
     */
    String createOrder(String accessToken, CreateOrderRequest requestPayload) {
        try {
            String body = PayPalResponseMapper.serializeCreateOrder(requestPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/checkout/orders"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PayPal order", e);
        }
    }

    /**
     * Captures a previously created PayPal order.
     *
     * @param accessToken   a valid OAuth2 bearer token.
     * @param paypalOrderId the PayPal-issued order ID to capture.
     * @return the raw JSON response body from PayPal.
     */
    String captureOrder(String accessToken, String paypalOrderId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/checkout/orders/" + paypalOrderId + "/capture"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("PayPal-Request-Id", java.util.UUID.randomUUID().toString())
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to capture PayPal order", e);
        }
    }

    /**
     * Refunds a PayPal capture transaction.
     *
     * @param accessToken a valid OAuth2 bearer token.
     * @param captureId   the PayPal-issued capture/transaction ID to refund.
     * @return the raw JSON response body from PayPal.
     */
    String refundCapture(String accessToken, String captureId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/payments/captures/" + captureId + "/refund"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    // Use a stable request ID based on the captureId to ensure idempotency across retries
                    .header("PayPal-Request-Id", "refund-" + captureId)
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to refund PayPal capture", e);
        }
    }
}

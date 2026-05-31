// Coupling Level: Stamp Coupling, Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: It is stamp-coupled to the custom DTO CreateOrderRequest in createOrder since it receives the custom request object as a parameter. It is data-coupled via simple strings for access tokens and order IDs.
// Reason for Cohesion: The class acts exclusively as the HTTP network client for the external PayPal API, with all of its methods serving to make outbound API requests (OAuth, create order, capture order).
package com.aims.subsystem.paypal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class PayPalBoundary {
    private final String baseUrl;
    private final HttpClient httpClient;

    PayPalBoundary(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

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

    String createOrder(String accessToken, CreateOrderRequest requestPayload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/checkout/orders"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestPayload.toJsonString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PayPal order", e);
        }
    }

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
}

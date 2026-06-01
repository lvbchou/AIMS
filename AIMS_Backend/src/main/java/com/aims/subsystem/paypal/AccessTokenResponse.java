// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: The methods pass and receive simple parameter types (takes a raw JSON String to parse and returns basic/primitive types via getters).
// Reason for Cohesion: The class has a single, well-defined function: parsing and representing the PayPal OAuth2 access token response.
/**
 * SOLID Principles Analysis:
 * - **SRP (Single Responsibility Principle) Violation**: The DTO contains internal parsing logic (`ObjectMapper` tree mapping) in `parseResponse()`. A DTO should be a pure data container; serialization and parsing logic should reside in the client or mapping layers.
 * 
 * **Improvement Direction**: Move JSON parsing out of the DTO into the `PayPalBoundary` or a dedicated mapper class.
 */
package com.aims.subsystem.paypal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

class AccessTokenResponse {

    private String accessToken;
    private long expiresIn;

    void parseResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            this.accessToken = rootNode.path("access_token").asText();
            this.expiresIn = rootNode.path("expires_in").asLong();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PayPal access token response", e);
        }
    }

    String getAccessToken() {
        return this.accessToken;
    }

    long getExpiresIn() {
        return this.expiresIn;
    }
}

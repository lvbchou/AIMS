// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: It interacts via simple input data passing (a JSON String in parseResponse) and returns basic types through getters, remaining decoupled from domain entities.
// Reason for Cohesion: The entire class is dedicated solely to parsing and reading the PayPal order creation response payload, extracting only key details like the order ID and approval link.
/**
 * SOLID Principles Analysis:
 * - **SRP (Single Responsibility Principle) Violation**: Mixes data modeling with JSON structural parsing in `parseResponse()`.
 * 
 * **Improvement Direction**: Delegate the parsing concern to a JSON deserializer or HTTP client adapter.
 */
package com.aims.subsystem.paypal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateOrderResponse {

    private String paypalOrderId;
    private String status;
    private String approveUrl;

    // Error fields
    private String errorName;
    private String errorMessage;
    private String errorDebugId;

    public void parseResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            this.paypalOrderId = rootNode.path("id").asText(null);
            this.status = rootNode.path("status").asText(null);

            this.errorName = rootNode.path("name").asText(null);
            this.errorMessage = rootNode.path("message").asText(null);
            this.errorDebugId = rootNode.path("debug_id").asText(null);

            JsonNode links = rootNode.path("links");
            if (links.isArray()) {
                for (JsonNode link : links) {
                    String rel = link.path("rel").asText("");
                    if ("payer-action".equals(rel) || "approve".equals(rel)) {
                        this.approveUrl = link.path("href").asText(null);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PayPal create order response", e);
        }
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public String getStatus() {
        return status;
    }

    public String getApproveUrl() {
        return approveUrl;
    }

    public String getErrorName() {
        return errorName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorDebugId() {
        return errorDebugId;
    }
}

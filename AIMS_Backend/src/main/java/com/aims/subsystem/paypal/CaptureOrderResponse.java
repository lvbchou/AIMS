// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: It interacts with external callers using simple parameters (receives a JSON string in parseResponse and exposes basic string/boolean values via getters), keeping it decoupled from any complex domain models.
// Reason for Cohesion: Every method and field is solely dedicated to parsing and representing the specific properties and status (success/failure) of a captured PayPal transaction.
package com.aims.subsystem.paypal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CaptureOrderResponse {

    private String paypalOrderId;
    private String transactionId;
    private String status;

    private String errorName;
    private String errorMessage;
    private String errorDebugId;

    public void parseResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            // Parse success fields
            this.paypalOrderId = rootNode.path("id").asText(null);
            this.status = rootNode.path("status").asText(null);

            // Parse error fields if any
            this.errorName = rootNode.path("name").asText(null);
            this.errorMessage = rootNode.path("message").asText(null);
            this.errorDebugId = rootNode.path("debug_id").asText(null);

            JsonNode purchaseUnits = rootNode.path("purchase_units");
            if (purchaseUnits != null && purchaseUnits.isArray() && purchaseUnits.size() > 0) {
                JsonNode payments = purchaseUnits.get(0).path("payments");
                if (payments != null) {
                    JsonNode captures = payments.path("captures");
                    if (captures != null && captures.isArray() && captures.size() > 0) {
                        this.transactionId = captures.get(0).path("id").asText(null);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PayPal capture order response", e);
        }
    }

    public boolean checkSuccess() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
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

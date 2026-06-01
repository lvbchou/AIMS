// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: Its constructor accepts only basic primitive string attributes (amount, currency, urls) and produces a JSON string, ensuring it doesn't couple with domain models like Invoice or Order.
// Reason for Cohesion: Every field and method within this class strictly cooperates to format and serialize the JSON payload structure required by PayPal's Create Order API.
/**
 * SOLID Principles Analysis:
 * - **SRP (Single Responsibility Principle) Violation**: The class constructs its own JSON representation through `toJsonString()` using manual Jackson tree nodes. A DTO should only represent the data structure; serialization should be handled by the client or HTTP utility layers.
 * 
 * **Improvement Direction**: Use a standard JSON serialization library or let `PayPalBoundary` serialize the instance automatically using its JSON converter.
 */
package com.aims.subsystem.paypal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreateOrderRequest {
    private final String amount;
    private final String currencyCode;
    private final String invoiceId;
    private final String returnUrl;
    private final String cancelUrl;

    public CreateOrderRequest(String amount, String currencyCode, String invoiceId, String returnUrl,
            String cancelUrl) {
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.invoiceId = invoiceId;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
    }

    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();

            root.put("intent", "CAPTURE");

            // payment_source
            ObjectNode experienceContext = mapper.createObjectNode();
            experienceContext.put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED");
            experienceContext.put("landing_page", "LOGIN");
            experienceContext.put("user_action", "PAY_NOW");
            experienceContext.put("return_url", this.returnUrl);
            experienceContext.put("cancel_url", this.cancelUrl);

            ObjectNode paypal = mapper.createObjectNode();
            paypal.set("experience_context", experienceContext);

            ObjectNode paymentSource = mapper.createObjectNode();
            paymentSource.set("paypal", paypal);

            root.set("payment_source", paymentSource);

            // purchase_units
            ObjectNode amountNode = mapper.createObjectNode();
            amountNode.put("currency_code", this.currencyCode);
            amountNode.put("value", this.amount);

            ObjectNode purchaseUnit = mapper.createObjectNode();
            purchaseUnit.put("invoice_id", this.invoiceId);
            purchaseUnit.set("amount", amountNode);

            ArrayNode purchaseUnits = mapper.createArrayNode();
            purchaseUnits.add(purchaseUnit);

            root.set("purchase_units", purchaseUnits);

            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct CreateOrderRequest JSON", e);
        }
    }
}

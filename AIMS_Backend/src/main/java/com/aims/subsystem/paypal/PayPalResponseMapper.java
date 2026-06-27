package com.aims.subsystem.paypal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * PayPalResponseMapper — package-private static utility that centralises all
 * JSON parsing for PayPal API responses.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>SRP (Single Responsibility Principle):</strong> The three DTO
 * classes ({@link AccessTokenResponse}, {@link CreateOrderResponse},
 * {@link CaptureOrderResponse}) previously each owned an {@code ObjectMapper}
 * instance and contained {@code parseResponse()} methods.  This violated SRP
 * because a DTO's sole responsibility is to carry data, not to perform
 * serialisation.  All parsing logic is now centralised here.</p>
 *
 * <p><strong>Performance:</strong> {@code ObjectMapper} is heavyweight and
 * thread-safe; it is designed to be created once and shared.  The previous
 * design recreated it on every parse call.  This class holds a single
 * {@code static final} instance.</p>
 *
 * <p>Package-private visibility intentionally limits this class to the PayPal
 * subsystem — it is an internal implementation detail, not a public API.</p>
 */
class PayPalResponseMapper {

    /** Shared, thread-safe Jackson mapper — created once for the lifetime of the application. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Utility class — no instances needed. */
    private PayPalResponseMapper() {}

    // -------------------------------------------------------------------------
    // Serialization (R4 — moved from CreateOrderRequest.toJsonString)
    // -------------------------------------------------------------------------

    /**
     * Serializes a {@link CreateOrderRequest} into the nested JSON payload
     * expected by the PayPal {@code POST /v2/checkout/orders} endpoint.
     *
     * <p>The PayPal payload cannot be produced by direct field-to-JSON mapping
     * because it requires a deeply nested {@code payment_source / paypal /
     * experience_context} structure. This method builds that tree using the
     * shared {@link #MAPPER}, avoiding the per-call {@code ObjectMapper}
     * construction that previously existed in {@code CreateOrderRequest.toJsonString()}.</p>
     *
     * @param request the order parameters to serialize.
     * @return the JSON string to send as the HTTP request body.
     * @throws RuntimeException if serialization fails.
     */
    static String serializeCreateOrder(CreateOrderRequest request) {
        try {
            var root = MAPPER.createObjectNode();
            root.put("intent", "CAPTURE");

            // payment_source → paypal → experience_context
            var experienceContext = MAPPER.createObjectNode();
            experienceContext.put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED");
            experienceContext.put("landing_page", "LOGIN");
            experienceContext.put("user_action", "PAY_NOW");
            experienceContext.put("return_url", request.getReturnUrl());
            experienceContext.put("cancel_url", request.getCancelUrl());

            var paypal = MAPPER.createObjectNode();
            paypal.set("experience_context", experienceContext);

            var paymentSource = MAPPER.createObjectNode();
            paymentSource.set("paypal", paypal);

            root.set("payment_source", paymentSource);

            // purchase_units
            var amountNode = MAPPER.createObjectNode();
            amountNode.put("currency_code", request.getCurrencyCode());
            amountNode.put("value", request.getAmount());

            var purchaseUnit = MAPPER.createObjectNode();
            purchaseUnit.put("invoice_id", request.getInvoiceId());
            purchaseUnit.set("amount", amountNode);

            var purchaseUnits = MAPPER.createArrayNode();
            purchaseUnits.add(purchaseUnit);

            root.set("purchase_units", purchaseUnits);

            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize CreateOrderRequest", e);
        }
    }



    /**
     * Parses a PayPal OAuth2 access-token JSON response into an
     * {@link AccessTokenResponse}.
     *
     * <p>Expected JSON structure:
     * <pre>{@code
     * {
     *   "access_token": "...",
     *   "expires_in":   32400
     * }
     * }</pre>
     *
     * @param json the raw JSON string returned by PayPal.
     * @return a populated {@link AccessTokenResponse}.
     * @throws RuntimeException if the JSON cannot be parsed.
     */
    static AccessTokenResponse parseAccessToken(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            String accessToken = root.path("access_token").asText();
            long expiresIn = root.path("expires_in").asLong();
            return new AccessTokenResponse(accessToken, expiresIn);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PayPal access token response", e);
        }
    }

    /**
     * Parses a PayPal Create Order JSON response into a
     * {@link CreateOrderResponse}.
     *
     * <p>On success the response contains {@code id}, {@code status}, and a
     * {@code links} array that includes the payer-action / approve URL.
     * On error the response contains {@code name}, {@code message}, and
     * {@code debug_id}.</p>
     *
     * @param json the raw JSON string returned by PayPal.
     * @return a populated {@link CreateOrderResponse}.
     * @throws RuntimeException if the JSON cannot be parsed.
     */
    static CreateOrderResponse parseCreateOrder(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);

            String paypalOrderId = root.path("id").asText(null);
            String status = root.path("status").asText(null);
            String errorName = root.path("name").asText(null);
            String errorMessage = root.path("message").asText(null);
            String errorDebugId = root.path("debug_id").asText(null);

            String approveUrl = null;
            JsonNode links = root.path("links");
            if (links.isArray()) {
                for (JsonNode link : links) {
                    String rel = link.path("rel").asText("");
                    if ("payer-action".equals(rel) || "approve".equals(rel)) {
                        approveUrl = link.path("href").asText(null);
                        break;
                    }
                }
            }

            return new CreateOrderResponse(paypalOrderId, status, approveUrl,
                    errorName, errorMessage, errorDebugId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PayPal create order response", e);
        }
    }

    /**
     * Parses a PayPal Capture Order JSON response into a
     * {@link CaptureOrderResponse}.
     *
     * <p>On success the response contains {@code id}, {@code status}, and a
     * nested {@code purchase_units[0].payments.captures[0].id} transaction ID.
     * On error the response contains {@code name}, {@code message}, and
     * {@code debug_id}.</p>
     *
     * @param json the raw JSON string returned by PayPal.
     * @return a populated {@link CaptureOrderResponse}.
     * @throws RuntimeException if the JSON cannot be parsed.
     */
    static CaptureOrderResponse parseCaptureOrder(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);

            String paypalOrderId = root.path("id").asText(null);
            String status = root.path("status").asText(null);
            String errorName = root.path("name").asText(null);
            String errorMessage = root.path("message").asText(null);
            String errorDebugId = root.path("debug_id").asText(null);

            String transactionId = null;
            JsonNode purchaseUnits = root.path("purchase_units");
            if (purchaseUnits.isArray() && purchaseUnits.size() > 0) {
                JsonNode captures = purchaseUnits.get(0).path("payments").path("captures");
                if (captures.isArray() && captures.size() > 0) {
                    transactionId = captures.get(0).path("id").asText(null);
                }
            }

            return new CaptureOrderResponse(paypalOrderId, transactionId, status,
                    errorName, errorMessage, errorDebugId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PayPal capture order response", e);
        }
    }

    /**
     * Parses a PayPal refund response JSON.
     *
     * @param json raw JSON string returned by PayPal.
     * @return a populated {@link RefundResponse}.
     */
    static RefundResponse parseRefund(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            String refundId = root.path("id").asText(null);
            String status = root.path("status").asText(null);
            String errorName = root.path("name").asText(null);
            String errorMessage = root.path("message").asText(null);
            String errorDebugId = root.path("debug_id").asText(null);
            return new RefundResponse(refundId, status, errorName, errorMessage, errorDebugId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PayPal refund response", e);
        }
    }
}

package com.aims.subsystem.vietqr;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.aims.entity.PaymentResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This parser keeps checksum verification, order resolution, and payment result
 * conversion in a single callback-processing responsibility.
 *
 * @author Team 03
 * @since 1.0.0
 */
public class VietQRCallbackHandler {

    private final String callbackData;
    private final ObjectMapper objectMapper;

    public VietQRCallbackHandler(String callbackData) {
        this.callbackData = callbackData;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Verifies callback validity using a checksum.
     *
     * @param apiKey API key used to compute the checksum.
     * @return {@code true} if the checksum is valid or a sandbox callback omits it.
     */
    public boolean verifyChecksum(String apiKey) {
        if (callbackData == null || callbackData.isBlank()) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(callbackData);
            String received = firstText(root, "checksum");
            if (received == null || received.isBlank()) {
                // Sandbox / test callbacks may omit checksum
                return true;
            }
            if (apiKey == null || apiKey.isBlank()) {
                return false;
            }
            String expected = computeChecksum(root, apiKey);
            return received.equalsIgnoreCase(expected);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the orderId from the transfer content.
     *
     * @return the normalized orderId, or {@code null} if it cannot be resolved.
     */
    public String getOrderIdFromContent() {
        try {
            JsonNode root = objectMapper.readTree(callbackData);
            String content = firstNonEmpty(
                    firstText(root, "content"),
                    firstText(root, "description"),
                    firstText(root, "transactionContent"),
                    firstText(root, "orderId"));
            if (content == null) {
                return null;
            }

            String trimmed = content.trim();
            // Backward compat: old format "Order #ORD-001"
            if (trimmed.startsWith("Order #")) {
                return trimmed.substring("Order #".length()).trim();
            }
            // VietQR terminal prefix: "VQRxxxxxxxx ORD001" — take last space-delimited token
            if (trimmed.startsWith("VQR") && trimmed.contains(" ")) {
                String raw = trimmed.substring(trimmed.lastIndexOf(' ') + 1).trim();
                return restoreOrderId(raw);
            }
            // Plain orderId — restore dash if VietQR stripped it
            return restoreOrderId(trimmed);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Restores the orderId format when VietQR has stripped the dash.
     *
     * @param raw raw input string.
     * @return the normalized orderId, or the original value if it does not match the pattern.
     */
    private static String restoreOrderId(String raw) {
        if (raw == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([A-Za-z]+)([0-9]+)$").matcher(raw.trim());
        if (m.matches()) {
            return m.group(1) + "-" + m.group(2);
        }
        return raw;
    }

    /**
     * Converts the callback payload into a payment result object.
     *
     * @return the parsed {@link PaymentResult}.
     */
    public PaymentResult toPaymentResult() {
        String orderId = getOrderIdFromContent();
        try {
            JsonNode root = objectMapper.readTree(callbackData);
            String status = firstText(root, "paymentStatus");
            String transactionId = firstText(root, "transactionId");
            int successFlag = isPaymentSuccessful(status) ? 1 : 0;
            return new PaymentResult(status, "VietQR Callback Payload", orderId, transactionId, successFlag);
        } catch (Exception e) {
            return new PaymentResult("FAILED", "Callback parse error: " + e.getMessage(), orderId, null, 0);
        }
    }

    /**
     * Quickly checks whether the payment status is successful.
     *
     * @return {@code true} if the callback reports success.
     */
    public boolean checkSuccess() {
        try {
            JsonNode root = objectMapper.readTree(callbackData);
            String status = firstText(root, "paymentStatus");
            return isPaymentSuccessful(status);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Normalizes the success state extracted from the callback.
     *
     * @param status raw status from the payload.
     * @return {@code true} if the status represents success.
     */
    private static boolean isPaymentSuccessful(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        return "SUCCESS".equalsIgnoreCase(status) || "00".equals(status);
    }

    private String computeChecksum(JsonNode root, String apiKey) throws Exception {
        String payload = firstText(root, "content", "description", "transactionContent", "orderId")
                + root.path("amount").asText("")
                + firstText(root, "transactionId");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    private String firstText(JsonNode root, String... fieldNames) {
        if (root == null || fieldNames == null) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JsonNode node = root.path(fieldName);
            if (!node.isMissingNode()) {
                String value = node.asText(null);
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
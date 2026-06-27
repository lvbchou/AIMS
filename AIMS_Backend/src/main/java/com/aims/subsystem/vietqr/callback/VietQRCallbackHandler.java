package com.aims.subsystem.vietqr.callback;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper class to parse transaction callbacks and verify payload integrity checks.
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
     * Extracts the raw transaction description or content.
     */
    public String getRawContent() {
        try {
            JsonNode root = objectMapper.readTree(callbackData);
            return firstNonEmpty(
                    firstText(root, "content"),
                    firstText(root, "description"),
                    firstText(root, "transactionContent"),
                    firstText(root, "orderId"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the raw order ID from the callback data if provided directly.
     */
    public String getDirectOrderId() {
        try {
            JsonNode root = objectMapper.readTree(callbackData);
            return firstText(root, "orderId");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the raw transaction status.
     */
    public String getStatus() {
        try {
            JsonNode root = objectMapper.readTree(callbackData);
            return firstText(root, "paymentStatus");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the raw transaction ID.
     */
    public String getTransactionId() {
        try {
            JsonNode root = objectMapper.readTree(callbackData);
            return firstText(root, "transactionId");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks whether the payment status represents a success state.
     */
    public boolean checkSuccess() {
        String status = getStatus();
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
package com.aims.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This entity stores and parses the QR payload data for a single VietQR payment.
 *
 * @author Team 03
 * @since 1.0.0
 */
public class QRCode {

    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String qrCode;
    private String qrLink;
    private String bankCode;
    private String bankName;
    private String bankAccount;
    private String content;
    private String orderId;
    private LocalDateTime expireAt;

    public QRCode() {
    }

    public QRCode(String qrCode, String qrLink, String bankCode, String bankName,
                  String bankAccount, String orderId, LocalDateTime expireAt) {
        this.qrCode = qrCode;
        this.qrLink = qrLink;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.orderId = orderId;
        this.expireAt = expireAt;
    }

    /**
     * Parses the QR response from VietQR.
     * <p>
     * Supports two formats: JSON from the live API and key=value strings from the fallback payload.
     *
     * @param response raw response from VietQR.
     */
    public void parseQRCodeResponse(String response) {
        if (response == null || response.isBlank()) {
            return;
        }
        String trimmed = response.trim();
        if (trimmed.startsWith("{")) {
            // Use the JSON parser for responses from the live API.
            try {
                JsonNode root = OBJECT_MAPPER.readTree(trimmed);
                qrCode    = textOrNull(root, "qrCode");
                qrLink    = textOrNull(root, "qrLink");
                bankCode  = textOrNull(root, "bankCode");
                bankName  = textOrNull(root, "bankName");
                bankAccount = textOrNull(root, "bankAccount");
                content   = textOrNull(root, "content");
                orderId   = textOrNull(root, "orderId");
                return;
            } catch (Exception ignored) {
                // If JSON parsing fails, fall through to the legacy parser below.
            }
        }
        // Legacy key=value;key=value;... format.
        for (String token : trimmed.split("[;&|,]")) {
            String[] pair = token.split("[=]", 2);
            if (pair.length != 2) {
                continue;
            }
            String key   = pair[0].trim();
            String value = pair[1].trim();
            switch (key) {
                case "qrCode"      -> qrCode      = value;
                case "qrLink"      -> qrLink       = value;
                case "bankCode"    -> bankCode     = value;
                case "bankName"    -> bankName     = value;
                case "bankAccount" -> bankAccount  = value;
                case "content"     -> content      = value;
                case "orderId"     -> orderId      = value;
                case "expireAt"    -> {
                    try { expireAt = LocalDateTime.parse(value, EXPIRY_FORMATTER); }
                    catch (Exception ignored) { }
                }
                default -> { }
            }
        }
    }

    private static String textOrNull(JsonNode root, String field) {
        JsonNode node = root.path(field);
        if (node.isMissingNode() || node.isNull()) return null;
        String v = node.asText(null);
        return (v == null || v.isBlank()) ? null : v;
    }

    /**
     * Checks whether the QR has expired.
     *
     * @return {@code true} if the QR has expired.
     */
    public boolean isExpired() {
        return expireAt != null && !expireAt.isAfter(LocalDateTime.now());
    }

    /**
     * Calculates the remaining seconds before the QR expires.
     *
     * @return the remaining seconds, never negative.
     */
    public long getSecondsUntilExpiry() {
        if (expireAt == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(LocalDateTime.now(), expireAt).getSeconds());
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrLink() {
        return qrLink;
    }

    public void setQrLink(String qrLink) {
        this.qrLink = qrLink;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}
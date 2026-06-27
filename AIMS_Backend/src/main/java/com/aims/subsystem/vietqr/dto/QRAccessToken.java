package com.aims.subsystem.vietqr.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Data carrier class representing the access token response from VietQR.
 */
public class QRAccessToken {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "accessToken[:=]([^;&|,]+).*tokenType[:=]([^;&|,]+).*expiresIn[:=]([0-9]+)",
            Pattern.CASE_INSENSITIVE);

    private String accessToken;
    private String tokenType;
    private int expiresIn;

    public QRAccessToken() {
    }

    public QRAccessToken(String accessToken, String tokenType, int expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    /**
     * Parses the token response from JSON or a legacy text format.
     *
     * @param response raw response string from VietQR.
     */
    public void parseResponseString(String response) {
        if (response == null || response.isBlank()) {
            return;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(response);
            String parsedAccessToken = firstText(root, "accessToken", "token", "access_token");
            String parsedTokenType = firstText(root, "tokenType", "type", "token_type");
            int parsedExpiresIn = firstInt(root, "expiresIn", "expires", "expires_in");

            if (parsedAccessToken != null) {
                accessToken = parsedAccessToken;
                tokenType = parsedTokenType;
                expiresIn = parsedExpiresIn;
                return;
            }
        } catch (Exception ignored) {
            // Fall back to the legacy plain-text parser when JSON parsing fails.
        }

        Matcher matcher = TOKEN_PATTERN.matcher(response.replace('\n', ' '));
        if (matcher.find()) {
            accessToken = matcher.group(1).trim();
            tokenType = matcher.group(2).trim();
            expiresIn = Integer.parseInt(matcher.group(3).trim());
        }
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
                    return value;
                }
            }
        }
        return null;
    }

    private int firstInt(JsonNode root, String... fieldNames) {
        if (root == null || fieldNames == null) {
            return 0;
        }
        for (String fieldName : fieldNames) {
            JsonNode node = root.path(fieldName);
            if (!node.isMissingNode() && node.isNumber()) {
                return node.asInt();
            }
            if (!node.isMissingNode()) {
                String value = node.asText(null);
                if (value != null && !value.isBlank()) {
                    try {
                        return Integer.parseInt(value.trim());
                    } catch (NumberFormatException ignored) {
                        // Try the next alias.
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Returns the parsed access token.
     *
     * @return the access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Updates the parsed access token.
     *
     * @param accessToken new token value.
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Returns the token type.
     *
     * @return the token type.
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Updates the token type.
     *
     * @param tokenType new token type.
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * Returns the token lifetime.
     *
     * @return the remaining lifetime in seconds.
     */
    public int getExpiresIn() {
        return expiresIn;
    }

    /**
     * Updates the token lifetime.
     *
     * @param expiresIn remaining lifetime in seconds.
     */
    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}

package com.aims.subsystem.vietqr;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This boundary isolates HTTP request construction, transport, and fallback
 * handling for the VietQR integration.
 *
 * @author Team 03
 * @since 1.0.0
 */
@Component
public class VietQRBoundary {

    private static final Logger log = LoggerFactory.getLogger(VietQRBoundary.class);

    private static final String TOKEN_ENDPOINT = "/api/token_generate";
    private static final String QR_ENDPOINT = "/api/qr/generate-customer";
    private final String host;
    private final String basePath;
    private final HttpClient httpClient;
    private final Duration httpTimeout;

    
    @Value("${vietqr.merchant-id:customer-testaimvd-user26586}")
    private String merchantId;

    @Value("${vietqr.api-key:Y3VzdG9tZXItdGVzdGFpbXZkLXVzZXIyNjU4Ng==}")
    private String apiKey;

    @Value("${vietqr.bank-code:970418}")   
    private String bankCode;

    @Value("${vietqr.bank-account:8823302684}")
    private String bankAccount;

    @Value("${vietqr.bank-name:BIDV}")
    private String bankName;

    public VietQRBoundary(
            @Value("${vietqr.host:https://dev.vietqr.org}") String host,
            @Value("${vietqr.base-path:/vqr}") String basePath,
            @Value("${vietqr.http-timeout-seconds:10}") int httpTimeoutSeconds) {
        this.host = normalizeHost(host);
        this.basePath = normalizeBasePath(basePath);
        Duration timeout = Duration.ofSeconds(Math.max(1, httpTimeoutSeconds));
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        this.httpTimeout = timeout;
    }

    /**
     * Requests an access token from VietQR.
     *
     * @param request authentication data used to obtain the token.
     * @return the parsed VietQR token.
     */
    public QRAccessToken requestAccessToken(QRAccessTokenRequest request) {
        try {
            return getAccessTokenResponse(request.buildAuthorizationHeader());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error getting access token from VietQR: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the access token string from the authentication request.
     *
     * @param request request containing merchantId and apiKey.
     * @return the access token, or an empty string if the token is missing.
     */
    public String getAccessToken(QRAccessTokenRequest request) {
        QRAccessToken token = requestAccessToken(request);
        if (token.getAccessToken() != null && !token.getAccessToken().isBlank()) {
            return token.getAccessToken();
        }
        return "";
    }

    /** Returns the configured merchant ID. */
    public String getMerchantId() {
        return merchantId;
    }

    /** Returns the configured API key. */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sends a token request using a prebuilt Authorization header.
     *
     * @param authorizationHeader VietQR-compliant Authorization header.
     * @return the parsed VietQR token.
     * @throws IOException if an I/O error occurs during the HTTP call.
     * @throws InterruptedException if the request is interrupted.
     */
    public QRAccessToken getAccessTokenResponse(String authorizationHeader) throws IOException, InterruptedException {
        log.info("Calling VietQR token endpoint: {}{}{}", host, basePath, TOKEN_ENDPOINT);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(TOKEN_ENDPOINT)))
                .timeout(httpTimeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", authorizationHeader)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("VietQR token endpoint response: status={}", response.statusCode());
        log.debug("VietQR token endpoint response body: {}", response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("VietQR token request failed with status " + response.statusCode() + ": " + response.body());
        }

        QRAccessToken token = new QRAccessToken();
        token.parseResponseString(response.body());
        return token;
    }

    /**
     * Returns the access token string from the VietQR API.
     *
     * @param authorizationHeader Basic Authorization header value.
     * @return the access token, or an empty string if the response does not contain one.
     * @throws IOException if the HTTP request fails.
     * @throws InterruptedException if the request is interrupted.
     */
    public String getAccessToken(String authorizationHeader) throws IOException, InterruptedException {
        QRAccessToken token = getAccessTokenResponse(authorizationHeader);
        if (token.getAccessToken() != null && !token.getAccessToken().isBlank()) {
            return token.getAccessToken();
        }
        return "";
    }

    /**
     * Creates a QR request from order data and configured bank details.
     *
     * @param content transfer content.
     * @param amount payment amount.
     * @param orderId order identifier.
     * @param userBankName recipient name shown on the QR.
     * @return a QR request populated with data.
     */
    public QRGenerateRequest createGenerateRequest(String content, long amount, String orderId, String userBankName) {
        QRGenerateRequest request = new QRGenerateRequest();
        request.setBankAccount(bankAccount);
        request.setBankName(bankName);
        request.setAmount(amount);
        request.setContent(content);
        request.setBankCode(bankCode);
        request.setOrderId(orderId);
        request.setUserBankName(userBankName);
        return request;
    }

    /**
     * Generates a QR payload, preferring the live API before falling back to a static URL.
     *
     * @param request QR request to generate.
     * @return a QR payload or fallback URL.
     */
    public String generateQrCode(QRGenerateRequest request) {
        try {
            QRAccessTokenRequest tokenRequest = new QRAccessTokenRequest(merchantId, apiKey);
            String accessToken = getAccessToken(tokenRequest.buildAuthorizationHeader());
            if (accessToken != null && !accessToken.isBlank()) {
                String apiResponse = callGenerateCustomerApi(request, accessToken);
                if (apiResponse != null && !apiResponse.isBlank()) {
                    return apiResponse;
                }
            }
        } catch (Exception e) {
            log.warn("VietQR generate API unavailable, falling back to static QR URL: {}", e.getMessage());
        }
        return buildStaticQrPayload(request);
    }

    /**
     * Sends a QR generation request to VietQR with fallback support.
     *
     * @param request QR request to send.
     * @return the QR payload or fallback data.
     */
    public String getQRCode(QRGenerateRequest request) {
        try {
            String accessToken = request.getAccessToken();
            if (accessToken != null && !accessToken.isBlank()) {
                String apiResponse = callGenerateCustomerApi(request, accessToken);
                if (apiResponse != null && !apiResponse.isBlank()) {
                    return apiResponse;
                }
            }
        } catch (Exception e) {
            log.warn("VietQR generate API unavailable, falling back to static QR URL: {}", e.getMessage());
        }
        return buildStaticQrPayload(request);
    }

    private String callGenerateCustomerApi(QRGenerateRequest request, String accessToken)
            throws IOException, InterruptedException {
        request.setAccessToken(accessToken);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(QR_ENDPOINT)))
                .timeout(httpTimeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(request.buildRequestString()))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        log.info("VietQR generate endpoint response: status={}", response.statusCode());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.debug("VietQR generate response body: {}", response.body());
            return null;
        }
        return response.body();
    }

    private String buildStaticQrPayload(QRGenerateRequest request) {
        String qrImageUrl = buildStaticQrImageUrl(request);
        return "qrCode=" + qrImageUrl
                + ";qrLink=" + qrImageUrl
                + ";bankCode=" + safeValue(request.getBankCode())
                + ";bankName=" + safeValue(request.getBankName())
                + ";bankAccount=" + safeValue(request.getBankAccount())
                + ";content=" + safeValue(request.getContent())
                + ";orderId=" + safeValue(request.getOrderId());
    }

    private String buildUrl(String endpoint) {
        return host + basePath + endpoint;
    }

    private String buildStaticQrImageUrl(QRGenerateRequest request) {
        String accountName = safeValue(request.getUserBankName() != null ? request.getUserBankName() : bankName);
        String content = safeValue(request.getContent());
        return "https://img.vietqr.io/image/"
                + safeValue(request.getBankCode()) + "-"
                + safeValue(request.getBankAccount()) + "-compact2.jpg"
                + "?amount=" + request.getAmount()
                + "&addInfo=" + encode(content)
                + "&accountName=" + encode(accountName);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String normalizeHost(String value) {
        if (value == null || value.isBlank()) {
            return "https://dev.vietqr.org";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizeBasePath(String value) {
        if (value == null || value.isBlank()) {
            return "/vqr";
        }
        String normalized = value.startsWith("/") ? value : "/" + value;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}

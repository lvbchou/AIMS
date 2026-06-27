package com.aims.subsystem.vietqr;

import com.aims.subsystem.vietqr.config.VietQRProperties;
import com.aims.subsystem.vietqr.dto.QRAccessToken;
import com.aims.subsystem.vietqr.client.VietQRHttpClient;
import com.aims.subsystem.vietqr.client.VietQRRequestBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Concrete implementation of IVietQRBoundary interface.
 * Coordinates outbound HTTP communications with VietQR API endpoints.
 */
@Component
public class VietQRBoundary implements IVietQRBoundary {

    private static final Logger log = LoggerFactory.getLogger(VietQRBoundary.class);

    private final VietQRProperties properties;
    private final VietQRHttpClient httpClient;
    private final VietQRRequestBuilder requestBuilder;

    public VietQRBoundary(
            VietQRProperties properties,
            VietQRHttpClient httpClient,
            VietQRRequestBuilder requestBuilder) {
        this.properties = properties;
        this.httpClient = httpClient;
        this.requestBuilder = requestBuilder;
    }

    @Override
    public QRAccessToken requestAccessToken() {
        String url = properties.getHost() + properties.getBasePath() + "/api/token_generate";
        
        // Construct Basic Auth header
        String credentials = properties.getMerchantId() + ":" + properties.getApiKey();
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        
        log.debug("Requesting access token: merchantId={}, apiKey={}, authHeader={}", 
                 properties.getMerchantId(), properties.getApiKey(), authHeader);
        try {
            String responseBody = httpClient.post(url, authHeader, "");
            QRAccessToken token = new QRAccessToken();
            token.parseResponseString(responseBody);
            return token;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error getting access token from VietQR: " + e.getMessage(), e);
        }
    }

    @Override
    public String getQRCode(String content, long amount, String orderId, String userBankName, String accessToken) {
        String url = properties.getHost() + properties.getBasePath() + "/api/qr/generate-customer";
        String authHeader = "Bearer " + accessToken;
        String body = requestBuilder.buildGenerateRequest(
                content,
                amount,
                orderId,
                properties.getBankCode(),
                properties.getBankAccount(),
                userBankName
        );
        try {
            return httpClient.post(url, authHeader, body);
        } catch (Exception e) {
            log.error("VietQR generate API unavailable", e);
            throw new RuntimeException("VietQR generate API unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public String callTestCallbackApi(
            String content,
            long amount,
            String transType,
            String accessToken) throws IOException, InterruptedException {
        String url = properties.getHost() + properties.getBasePath() + "/bank/api/test/transaction-callback";
        String authHeader = "Bearer " + accessToken;
        String body = requestBuilder.buildTestCallbackRequest(
                properties.getBankAccount(),
                content,
                amount,
                transType,
                properties.getBankCode()
        );
        return httpClient.post(url, authHeader, body);
    }

    @Override
    public String getBankCode() {
        return properties.getBankCode();
    }

    @Override
    public String getBankAccount() {
        return properties.getBankAccount();
    }
}

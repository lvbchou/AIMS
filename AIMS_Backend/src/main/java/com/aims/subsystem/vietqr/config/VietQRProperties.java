package com.aims.subsystem.vietqr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration properties class mapping connection settings and merchant accounts for VietQR.
 */
@Component
public class VietQRProperties {

    private final String host;
    private final String basePath;
    private final int httpTimeoutSeconds;

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

    public VietQRProperties(
            @Value("${vietqr.host:https://dev.vietqr.org}") String host,
            @Value("${vietqr.base-path:/vqr}") String basePath,
            @Value("${vietqr.http-timeout-seconds:10}") int httpTimeoutSeconds) {
        this.host = normalizeHost(host);
        this.basePath = normalizeBasePath(basePath);
        this.httpTimeoutSeconds = Math.max(1, httpTimeoutSeconds);
    }

    public String getHost() {
        return host;
    }

    public String getBasePath() {
        return basePath;
    }

    public int getHttpTimeoutSeconds() {
        return httpTimeoutSeconds;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getBankName() {
        return bankName;
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

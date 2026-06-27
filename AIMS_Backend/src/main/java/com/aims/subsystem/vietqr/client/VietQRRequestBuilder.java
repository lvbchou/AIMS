package com.aims.subsystem.vietqr.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Request payload builder helper to construct JSON bodies for VietQR APIs.
 */
@Component
public class VietQRRequestBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildGenerateRequest(
            String content,
            long amount,
            String orderId,
            String bankCode,
            String bankAccount,
            String userBankName) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "content", content,
                    "amount", amount,
                    "orderId", orderId,
                    "bankCode", bankCode,
                    "bankAccount", bankAccount,
                    "userBankName", userBankName
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize QR generate request payload", e);
        }
    }

    public String buildTestCallbackRequest(
            String bankAccount,
            String content,
            long amount,
            String transType,
            String bankCode) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "bankAccount", bankAccount,
                    "content", content,
                    "amount", amount,
                    "transType", transType,
                    "bankCode", bankCode
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize test callback request payload", e);
        }
    }
}

package com.aims.subsystem.vietqr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This request object carries only the fields required to generate a VietQR
 * payment payload.
 *
 * @author Team 03
 * @since 1.0.0
 */
public class QRGenerateRequest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String content;
    private long amount;
    /** Not sent to the VietQR API; used only for static QR fallback. */
    @JsonIgnore
    private String bankName;
    private String orderId;
    private String bankCode;
    private String bankAccount;
    private String userBankName;
    private final int qrType = 0;
    private final String transType = "C";
    /** Not included in the request body; used only for the Bearer Authorization header. */
    @JsonIgnore
    private String accessToken;

    public QRGenerateRequest() {
    }

    public QRGenerateRequest(String content, long amount, String orderId, String bankCode,
                             String bankAccount, String userBankName, String accessToken) {
        this.content = content;
        this.amount = amount;
        this.orderId = orderId;
        this.bankCode = bankCode;
        this.bankAccount = bankAccount;
        this.userBankName = userBankName;
        this.accessToken = accessToken;
    }

    /**
     * Returns the recipient bank name.
     *
     * @return the bank name.
     */
    public String getBankName() {
        return bankName;
    }

    /**
     * Updates the recipient bank name.
     *
     * @param bankName bank name.
     */
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getContent() {
        return content;
    }

    public long getAmount() {
        return amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getUserBankName() {
        return userBankName;
    }

    public int getQrType() {
        return qrType;
    }

    public String getTransType() {
        return transType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public void setUserBankName(String userBankName) {
        this.userBankName = userBankName;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Serializes the QR request to JSON.
     *
     * @return the serialized JSON string.
     */
    public String buildRequestString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing VietQR request", e);
        }
    }
}
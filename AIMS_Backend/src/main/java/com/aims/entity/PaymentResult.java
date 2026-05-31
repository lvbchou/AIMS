package com.aims.entity;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This entity represents the parsed outcome of a VietQR callback and keeps the
 * callback result data together in one responsibility.
 *
 * @author Team 03
 * @since 1.0.0
 */
public class PaymentResult {

    private String status;
    private String message;
    private String orderId;
    private String paymentId;
    private int success;

    public PaymentResult() {
    }

    /**
     * Creates a callback result with full details.
     *
     * @param status payment status.
     * @param message processing message.
     * @param orderId order identifier.
     * @param paymentId transaction identifier.
     * @param success success flag, where a positive value indicates success.
     */
    public PaymentResult(String status, String message, String orderId, String paymentId, int success) {
        this.status = status;
        this.message = message;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.success = success;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Checks whether the callback was successful.
     *
     * @return {@code true} if the status or success flag indicates a valid success.
     */
    public boolean checkSuccess() {
        return success > 0 || "SUCCESS".equalsIgnoreCase(status);
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSuccess(int success) {
        this.success = success;
    }
}
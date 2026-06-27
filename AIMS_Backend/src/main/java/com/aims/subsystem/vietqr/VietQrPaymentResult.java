package com.aims.subsystem.vietqr;

/**
 * VietQrPaymentResult — represents the parsed outcome of a VietQR payment
 * callback.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>SRP (Single Responsibility Principle):</strong> The previous
 * class was named {@code PaymentResult} and resided in
 * {@code com.aims.entity} — a generic name and location implying it was a
 * shared abstraction.  However, its internal implementation (the
 * {@code checkSuccess} method and field semantics) was tightly coupled to the
 * VietQR callback format.</p>
 *
 * <p>Renaming this class to {@code VietQrPaymentResult} and moving it to the
 * VietQR subsystem package makes the scope explicit.  If a different gateway
 * (PayPal, Stripe) needs a callback result class, it creates its own
 * gateway-specific type — it does not modify or reuse this one.</p>
 *
 * <p><strong>OCP (Open/Closed Principle):</strong> VietQR-specific success
 * determination logic ({@code checkSuccess}) is now encapsulated here.  A new
 * gateway's different success semantics are implemented in its own class,
 * not by modifying this one.</p>
 */
public class VietQrPaymentResult {

    private String status;
    private String message;
    private String orderId;
    private String paymentId;
    private int success;

    public VietQrPaymentResult() {
    }

    /**
     * Creates a callback result with full details.
     *
     * @param status    VietQR payment status string.
     * @param message   processing message.
     * @param orderId   order identifier.
     * @param paymentId transaction identifier.
     * @param success   success flag, where a positive value indicates success.
     */
    public VietQrPaymentResult(String status, String message, String orderId,
                                String paymentId, int success) {
        this.status = status;
        this.message = message;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.success = success;
    }

    /**
     * Checks whether the VietQR callback indicates a successful payment.
     *
     * <p>VietQR signals success either through a positive {@code success}
     * integer or through the literal status string {@code "SUCCESS"}.</p>
     *
     * @return {@code true} if the payment was successful; {@code false} otherwise.
     */
    public boolean checkSuccess() {
        return success > 0 || "SUCCESS".equalsIgnoreCase(status);
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getPaymentId() {
        return paymentId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
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

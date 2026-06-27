package com.aims.subsystem.paypal;

/**
 * RefundResponse — immutable package-private data holder for a PayPal Capture Refund response.
 */
class RefundResponse {

    private final String refundId;
    private final String status;
    private final String errorName;
    private final String errorMessage;
    private final String errorDebugId;

    public RefundResponse(String refundId, String status, String errorName, String errorMessage, String errorDebugId) {
        this.refundId = refundId;
        this.status = status;
        this.errorName = errorName;
        this.errorMessage = errorMessage;
        this.errorDebugId = errorDebugId;
    }

    public boolean checkSuccess() {
        // PayPal refund is successful if we get a refund ID and status is COMPLETED or PENDING
        return refundId != null && (status == null || "COMPLETED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status));
    }

    public String getRefundId() {
        return refundId;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorName() {
        return errorName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorDebugId() {
        return errorDebugId;
    }
}

// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: Carries only simple primitive fields; all JSON parsing
//   has been moved to PayPalResponseMapper (SRP fix).
// Reason for Cohesion: Solely responsible for holding capture-order response data,
//   plus the domain check checkSuccess() which operates purely on the held data.
/**
 * SOLID Principles Analysis (refactored):
 * - **SRP Compliance**: Previously violated SRP by owning an ObjectMapper and
 *   a parseResponse() method. The DTO is now a pure data holder. All parsing
 *   logic lives in {@link PayPalResponseMapper}.
 * - **checkSuccess() retention**: This method is a domain check on the DTO's
 *   own data (is the status "COMPLETED"?), not a parsing concern — it stays here.
 */
package com.aims.subsystem.paypal;

/**
 * CaptureOrderResponse — immutable data holder for a PayPal Capture Order
 * response.
 *
 * <p>Instances are created exclusively by
 * {@link PayPalResponseMapper#parseCaptureOrder(String)}.</p>
 */
public class CaptureOrderResponse {

    private final String paypalOrderId;
    private final String transactionId;
    private final String status;
    private final String errorName;
    private final String errorMessage;
    private final String errorDebugId;

    /**
     * Constructs a fully populated capture-order response.
     *
     * @param paypalOrderId  the PayPal-issued order ID.
     * @param transactionId  the capture transaction ID (nested in purchase_units).
     * @param status         the capture status (e.g., {@code "COMPLETED"}).
     * @param errorName      the error name if the call failed; {@code null} on success.
     * @param errorMessage   the error message if the call failed; {@code null} on success.
     * @param errorDebugId   the PayPal debug ID for tracing; {@code null} on success.
     */
    public CaptureOrderResponse(String paypalOrderId, String transactionId, String status,
                                 String errorName, String errorMessage, String errorDebugId) {
        this.paypalOrderId = paypalOrderId;
        this.transactionId = transactionId;
        this.status = status;
        this.errorName = errorName;
        this.errorMessage = errorMessage;
        this.errorDebugId = errorDebugId;
    }

    /**
     * Returns {@code true} if the capture was successful.
     *
     * <p>PayPal signals a successful capture with status {@code "COMPLETED"}.
     * This method encapsulates that PayPal-specific success semantic and is
     * appropriately placed on the DTO that carries the status field.</p>
     *
     * @return {@code true} if {@code status} equals {@code "COMPLETED"} (case-insensitive).
     */
    public boolean checkSuccess() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public String getTransactionId() {
        return transactionId;
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

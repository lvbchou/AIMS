// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: Carries only simple primitive fields; all JSON parsing
//   has been moved to PayPalResponseMapper (SRP fix).
// Reason for Cohesion: Solely responsible for holding create-order response data.
/**
 * SOLID Principles Analysis (refactored):
 * - **SRP Compliance**: Previously violated SRP by owning an ObjectMapper and
 *   a parseResponse() method. The DTO is now a pure data holder. All parsing
 *   logic lives in {@link PayPalResponseMapper}.
 */
package com.aims.subsystem.paypal;

/**
 * CreateOrderResponse — immutable data holder for a PayPal Create Order response.
 *
 * <p>Instances are created exclusively by
 * {@link PayPalResponseMapper#parseCreateOrder(String)}.</p>
 */
public class CreateOrderResponse {

    private final String paypalOrderId;
    private final String status;
    private final String approveUrl;
    private final String errorName;
    private final String errorMessage;
    private final String errorDebugId;

    /**
     * Constructs a fully populated create-order response.
     *
     * @param paypalOrderId  the PayPal-issued order ID (success field).
     * @param status         the order status (e.g., {@code "CREATED"}).
     * @param approveUrl     the payer-action URL the customer must visit.
     * @param errorName      the error name if the call failed; {@code null} on success.
     * @param errorMessage   the error message if the call failed; {@code null} on success.
     * @param errorDebugId   the PayPal debug ID for tracing; {@code null} on success.
     */
    public CreateOrderResponse(String paypalOrderId, String status, String approveUrl,
                                String errorName, String errorMessage, String errorDebugId) {
        this.paypalOrderId = paypalOrderId;
        this.status = status;
        this.approveUrl = approveUrl;
        this.errorName = errorName;
        this.errorMessage = errorMessage;
        this.errorDebugId = errorDebugId;
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public String getStatus() {
        return status;
    }

    public String getApproveUrl() {
        return approveUrl;
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

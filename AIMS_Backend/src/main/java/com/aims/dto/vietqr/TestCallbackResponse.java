package com.aims.dto.vietqr;

/**
 * Response DTO for test/simulated VietQR callbacks.
 *
 * Moved from inner record inside {@code VietQRCallbackEndpoint}.
 *
 * @author Team 03
 * @since 1.0.0
 */
public record TestCallbackResponse(String status, String message) {

    /**
     * Creates a successful test response.
     *
     * @param message result message.
     * @return a success response.
     */
    public static TestCallbackResponse success(String message) {
        return new TestCallbackResponse("SUCCESS", message);
    }

    /**
     * Creates a failed test response.
     *
     * @param message error message.
     * @return a failure response.
     */
    public static TestCallbackResponse failed(String message) {
        return new TestCallbackResponse("FAILED", message);
    }
}

package com.aims.subsystem.paypal;

/**
 * PayPalMockMode — package-private constants for developer mock mode.
 *
 * <h3>Design Rationale (R5)</h3>
 * <p>Three interrelated sentinel strings were previously scattered across
 * {@link PayPalAuthManager} and {@link PayPalController}:
 * <ul>
 *   <li>{@code "AWmock"} — client ID prefix that signals mock mode</li>
 *   <li>{@code "MOCK-TOKEN-"} — sentinel token prefix returned by the auth manager</li>
 *   <li>{@code "MOCK-EC-"} — sentinel order ID prefix returned by the controller</li>
 * </ul>
 * Centralising them here ensures that if the sentinel format ever changes, only
 * this file needs to be updated — not every class that checks for mock mode.</p>
 *
 * <p>Package-private visibility intentionally limits this class to the PayPal
 * subsystem — it is an internal implementation detail, not a public API.</p>
 */
final class PayPalMockMode {

    /** Client ID prefix that activates developer mock mode. */
    static final String CLIENT_ID_PREFIX = "AWmock";

    /**
     * Sentinel prefix for mock OAuth tokens returned by {@link PayPalAuthManager}.
     * Callers detect mock mode by checking {@code token.startsWith(TOKEN_PREFIX)}.
     */
    static final String TOKEN_PREFIX = "MOCK-TOKEN-";

    /**
     * Sentinel prefix for mock PayPal order IDs returned by
     * {@link PayPalController#createPayment}.
     * Callers detect mock capture mode by checking
     * {@code paypalOrderId.startsWith(ORDER_ID_PREFIX)}.
     */
    static final String ORDER_ID_PREFIX = "MOCK-EC-";

    /** Utility class — no instances. */
    private PayPalMockMode() {}
}

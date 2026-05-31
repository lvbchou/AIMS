package com.aims.constants;

/**
 * Maps UC003 payment lifecycle to {@code aims.orders.status} CHECK constraint values.
 */
public final class OrderStatusValues {

    private OrderStatusValues() {
    }

    /** UC003 precondition: order is in payment phase (awaiting VietQR completion). */
    public static final String AWAITING_PAYMENT = "pending";

    /**
     * UC003 postcondition: payment succeeded — order awaits fulfillment (“Pending Processing” wording).
     * Persisted as {@code approved} per aims.orders constraint ({@code pending|approved|rejected|cancelled}).
     */
    public static final String PENDING_PROCESSING = "approved";
}

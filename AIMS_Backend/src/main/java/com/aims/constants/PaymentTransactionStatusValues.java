package com.aims.constants;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This class groups the status constants used only for payment transactions.
 *
 * @author Team 03
 * @since 1.0.0
 */
public final class PaymentTransactionStatusValues {

    private PaymentTransactionStatusValues() {
    }

    /** Transaction is pending processing. */
    public static final String PENDING = "pending";
    /** Transaction completed successfully. */
    public static final String SUCCESS = "success";
    /** Transaction failed. */
    public static final String FAILED = "failed";
}

package com.aims.factory;

import com.aims.entity.Invoice;
import com.aims.entity.PaymentMethod;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.TransactionStatus;

import java.time.LocalDateTime;

/**
 * PaymentTransactionFactory — centralises the creation of
 * {@link PaymentTransaction} instances.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>Factory Method Pattern (HFDP Chapter 4):</strong> The factory
 * method {@link #createPending} centralises object creation and isolates the
 * knowledge of how to build a specific type of {@link PaymentTransaction} from
 * the entity itself.  This follows the principle of <em>encapsulate what
 * varies</em> — new payment methods may require different initial field values,
 * and the factory is the single place where that variation lives.</p>
 *
 * <p><strong>SRP (Single Responsibility Principle):</strong> Previously,
 * {@link PaymentTransaction} held a static factory method
 * {@code pendingVietQr()} that encoded VietQR-specific business rules
 * (payment method enum value, content format) inside a generic JPA entity.
 * The entity had two reasons to change: schema changes and factory logic
 * changes.  Extracting the factory here gives each class a single reason to
 * change.</p>
 *
 * <p><strong>OCP (Open/Closed Principle):</strong> Adding a new payment
 * method (e.g., MoMo) requires no changes to {@link PaymentTransaction} or
 * this factory — callers simply pass a different {@link PaymentMethod} value,
 * or this factory gains a new overloaded method if needed.</p>
 */
public class PaymentTransactionFactory {

    /** Utility class — no instances. */
    private PaymentTransactionFactory() {}

    /**
     * Creates a new {@link PaymentTransaction} in the {@code pending} state
     * for any payment method.
     *
     * <p>The transaction is initialised with:</p>
     * <ul>
     *   <li>A freshly generated transaction ID (via
     *       {@link PaymentTransaction#newTransactionId()}).</li>
     *   <li>The given {@code invoice} as the associated invoice.</li>
     *   <li>Content formatted as {@code "Order #<orderId>"}.</li>
     *   <li>The specified {@link PaymentMethod}.</li>
     *   <li>Transaction time set to {@link LocalDateTime#now()}.</li>
     *   <li>Status set to {@link TransactionStatus#pending}.</li>
     * </ul>
     *
     * @param invoice  the invoice this transaction is associated with.
     * @param orderId  the order identifier, used to compose the content field.
     * @param method   the payment method for this transaction.
     * @return a new, unsaved {@link PaymentTransaction} in the pending state.
     */
    public static PaymentTransaction createPending(Invoice invoice, String orderId, PaymentMethod method) {
        PaymentTransaction txn = new PaymentTransaction();
        txn.setTransactionId(PaymentTransaction.newTransactionId());
        txn.setInvoice(invoice);
        txn.setContent("Order #" + orderId);
        txn.setMethod(method);
        txn.setTransactionTime(LocalDateTime.now());
        txn.setStatus(TransactionStatus.pending);
        return txn;
    }
}

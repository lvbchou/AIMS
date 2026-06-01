/**
 * SOLID Principles Analysis:
 * - **SRP/OCP (Single Responsibility & Open/Closed) Violation**: The core entity class contains a static factory method `pendingVietQr()` dedicated to a specific payment method. This tightly couples the entity to concrete payment subsystems and requires code modification whenever new payment methods are introduced.
 * 
 * **Improvement Direction**: Move the creation of pending transactions into their respective subsystem packages (e.g. VietQr implementation of IPaymentGateway) or a dedicated factory class (`PaymentTransactionFactory`).
 */
package com.aims.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.aims.constants.PaymentTransactionStatusValues;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This entity concentrates all state and lifecycle behavior for a payment
 * transaction record.
 *
 * @author Team 03
 * @since 1.0.0
 */
@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
public class PaymentTransaction {

    @Id
    @Column(name = "transaction_id", length = 45)
    private String transactionId;

    @Transient
    private long amount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "invoice_id", referencedColumnName = "invoice_id")
    private Invoice invoice;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private PaymentMethod method;

    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    public PaymentTransaction(String transactionId, long amount, PaymentMethod method, String content) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.method = method;
        this.content = content;
        this.transactionTime = LocalDateTime.now();
        this.status = TransactionStatus.pending;
    }

    @PrePersist
    public void assignIdIfMissing() {
        ensureTransactionId();
    }

    /**
     * Ensures the transaction has an identifier before persistence.
     *
     * @implNote This can be called before {@code repository.save()} or left to {@code @PrePersist}.
     */
    public void ensureTransactionId() {
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = newTransactionId();
        }
    }

    /**
     * Generates a new transaction ID in the system's standard format.
     *
     * @return a new transaction ID.
     */
    public static String newTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    /**
     * Creates a PENDING transaction for the VietQR payment flow.
     *
     * @param invoice invoice identifier.
     * @param orderId order identifier.
     * @return a new transaction in the pending-payment state.
     */
    public static PaymentTransaction pendingVietQr(Invoice invoice, String orderId) {
        PaymentTransaction txn = new PaymentTransaction();
        txn.setTransactionId(newTransactionId());
        txn.setInvoice(invoice);
        txn.setContent("Order #" + orderId);
        txn.setMethod(PaymentMethod.VIET_QR);
        txn.setTransactionTime(LocalDateTime.now());
        txn.setStatus(TransactionStatus.pending);
        return txn;
    }
}

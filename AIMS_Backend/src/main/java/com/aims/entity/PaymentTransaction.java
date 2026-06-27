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
 *
 * This entity concentrates all state and lifecycle behavior for a payment
 * transaction record.
 *
 * SOLID VIOLATION: Single Responsibility Principle (SRP)
 *
 * Problem: This entity class mixes data persistence responsibility with
 *   payment-method-specific factory logic. The static factory method
 *   pendingVietQr(Invoice, String) creates a transaction preconfigured for
 *   the VietQR payment flow, embedding VietQR-specific business rules
 *   (PaymentMethod.VIET_QR, content format "Order #orderId") inside a
 *   generic entity class.
 * Impact: Adding a new payment method (e.g. MoMo, ZaloPay) would require
 *   adding another static factory method to this entity, growing the class
 *   with unrelated payment-method-specific logic.
 * Improvement:
 *   - Extract a PaymentTransactionFactory class with method-specific factory methods
 *   - Or move factory methods into the respective subsystem packages
 *     (e.g. VietQrTransactionFactory in the vietqr package)
 *   - Keep PaymentTransaction as a pure data entity with no factory logic
 *
 * SOLID VIOLATION: Open/Closed Principle (OCP)
 *
 * Problem: The pendingVietQr factory method is hardcoded to one payment method.
 *   To support a new payment method, a new factory method must be added to
 *   this class (e.g. pendingMoMo, pendingPayPal), modifying the entity.
 * Impact: The entity class grows unboundedly as new payment methods are introduced.
 * Improvement:
 *   - Use a Factory Pattern with a PaymentTransactionFactory that accepts
 *     PaymentMethod as a parameter and returns the appropriate transaction
 *   - The entity class remains closed for modification
 *
 * SOLID: Liskov Substitution Principle (LSP) - Not Violated
 *
 * This entity does not participate in an inheritance hierarchy.
 *
 * SOLID: Interface Segregation Principle (ISP) - Not Violated
 *
 * This entity does not implement any interface. It is a JPA entity class.
 *
 * SOLID: Dependency Inversion Principle (DIP) - Not Violated
 *
 * This entity has no dependencies on high-level modules. It only depends
 * on JPA annotations and its own field types, which is appropriate for
 * a data entity in the persistence layer.
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

    // -------------------------------------------------------------------------
    // P2.3 Refactoring Note
    // -------------------------------------------------------------------------
    // The static factory method pendingVietQr(Invoice, String) has been removed.
    //
    // Reason: It encoded VietQR-specific business rules (PaymentMethod.VIET_QR,
    //   content format) inside a generic JPA entity, violating both SRP and OCP.
    //   Adding a new payment method (MoMo, ZaloPay) would have grown this entity
    //   with more unrelated factory methods.
    //
    // Replacement: Use com.aims.factory.PaymentTransactionFactory.createPending()
    //   which accepts a PaymentMethod parameter and is not tied to any specific
    //   payment provider.
    //
    // Call-site migration:
    //   BEFORE: PaymentTransaction.pendingVietQr(invoice, orderId)
    //   AFTER:  PaymentTransactionFactory.createPending(invoice, orderId, PaymentMethod.VIET_QR)
}

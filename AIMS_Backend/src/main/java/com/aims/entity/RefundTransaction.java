package com.aims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * RefundTransaction - represents a refund processed for a transaction.
 * Configured as a JPA entity mapped to aims.refund_transaction.
 */
@Entity
@Table(name = "refund_transaction", schema = "aims")
@Getter
@Setter
@NoArgsConstructor
public class RefundTransaction {

    @Id
    @Column(name = "refund_transaction_id", length = 45)
    private String refundTransactionId;

    @OneToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id", nullable = false)
    private PaymentTransaction paymentTransaction;

    @Column(name = "refund_time", nullable = false)
    private LocalDateTime refundTime;

    public RefundTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
        this.refundTransactionId = "REF-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        this.refundTime = LocalDateTime.now();
    }
}

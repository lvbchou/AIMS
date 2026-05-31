package com.aims.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RefundTransaction - represents a refund processed for a transaction.
 */
@Getter
@Setter
public class RefundTransaction {

    private String refundTransactionId;
    private PaymentTransaction paymentTransaction;
    private LocalDateTime refundTime;

    public RefundTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
        this.refundTransactionId = UUID.randomUUID().toString();
        this.refundTime = LocalDateTime.now();
    }
}

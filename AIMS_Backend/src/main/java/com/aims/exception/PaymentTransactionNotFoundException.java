package com.aims.exception;

public class PaymentTransactionNotFoundException extends RuntimeException {

    public PaymentTransactionNotFoundException(String transactionId) {
        super("Payment transaction not found: " + transactionId);
    }
}

package com.aims.exception;

public class PaymentAlreadyCompletedException extends RuntimeException {

    public PaymentAlreadyCompletedException(String orderId) {
        super("Payment already completed for order: " + orderId);
    }
}

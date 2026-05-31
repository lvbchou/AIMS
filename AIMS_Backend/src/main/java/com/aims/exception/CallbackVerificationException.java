package com.aims.exception;

import com.aims.exception.PaymentException;

public class CallbackVerificationException extends PaymentException {

    public CallbackVerificationException(String message) {
        super(message);
    }

    public CallbackVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
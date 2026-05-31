package com.aims.exception;

public class OrderNotPayableException extends RuntimeException {

    public OrderNotPayableException(String message) {
        super(message);
    }
}

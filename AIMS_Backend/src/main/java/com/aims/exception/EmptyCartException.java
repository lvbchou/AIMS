package com.aims.exception;

/**
 * EmptyCartException - thrown if a cart contains no items during order creation.
 */
public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String message) {
        super(message);
    }
}

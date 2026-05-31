package com.aims.exception;

/**
 * InvalidDeliveryException - thrown if delivery information is missing or invalid.
 */
public class InvalidDeliveryException extends RuntimeException {
    public InvalidDeliveryException(String message) {
        super(message);
    }
}

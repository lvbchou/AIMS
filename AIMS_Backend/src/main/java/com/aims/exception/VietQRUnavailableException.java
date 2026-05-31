package com.aims.exception;

public class VietQRUnavailableException extends RuntimeException {

    public VietQRUnavailableException(String message) {
        super(message);
    }

    public VietQRUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

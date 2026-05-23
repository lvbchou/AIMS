package com.aims.exception;

public class EmptySearchInputException extends RuntimeException {
    public EmptySearchInputException() {
        super("Please enter a product title or category");
    }
}
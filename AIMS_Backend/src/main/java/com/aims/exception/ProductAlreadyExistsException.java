package com.aims.exception;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String barcode) {
        super("Product with barcode already exists: " + barcode);
    }
}
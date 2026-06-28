package com.aims.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Integer productId) {
        super("Product not found with id: " + productId);
    }

    public ProductNotFoundException(String barcode) {
        super("Product not found with barcode: " + barcode);
    }
}
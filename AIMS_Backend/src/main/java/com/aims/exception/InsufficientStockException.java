package com.aims.exception;

import com.aims.dto.response.StockAvailabilityIssue;

import java.util.List;

public class InsufficientStockException extends RuntimeException {
    private final List<StockAvailabilityIssue> affectedItems;

    public InsufficientStockException(List<StockAvailabilityIssue> affectedItems) {
        super("Some products do not have enough stock. Please update your cart.");
        this.affectedItems = affectedItems;
    }

    public List<StockAvailabilityIssue> getAffectedItems() {
        return affectedItems;
    }
}

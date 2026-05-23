package com.aims.dto;

public record ProductSummaryDTO(
        Integer productId,
        String title,
        String productType,
        Long sellingPrice,
        String image
) {}
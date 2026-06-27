
package com.aims.dto.product;

public record ProductSummaryDTO(
        Integer productId,
        String title,
        String productType,
        Long sellingPrice,
        String image,
        Integer quantityInStock
) {}
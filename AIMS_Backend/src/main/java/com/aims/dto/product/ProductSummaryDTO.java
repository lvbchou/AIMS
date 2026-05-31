/**
 * ProductSummaryDTO
 *
 * Cohesion Level: Functional
 * Reason: All five fields serve a single purpose — product list display.
 *
 * Coupling:
 *   - Data coupling with ProductService: received as return type of getAllProducts().
 */
package com.aims.dto.product;

public record ProductSummaryDTO(
        Integer productId,
        String title,
        String productType,
        Long sellingPrice,
        String image
) {}
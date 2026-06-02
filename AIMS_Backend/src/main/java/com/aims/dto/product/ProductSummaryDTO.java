/**
 * SRP VIOLATION:
 * ProductService.searchProduct() builds ProductSummaryDTO inline with
 * new ProductSummaryDTO(...). This mixes service orchestration logic
 * with object-mapping concern — two separate reasons to change.
 *
 * Impact: Changes to the DTO structure force modification of the service class,
 * and changes to mapping logic are scattered across the codebase.
 *
 * Improvement: Move the construction into ProductMapper.toSummary(Product p).
 * The service calls ProductMapper.toSummary(p), keeping all mapping concerns
 * in one dedicated class.
 */

package com.aims.dto.product;

public record ProductSummaryDTO(
        Integer productId,
        String title,
        String productType,
        Long sellingPrice,
        String image,
        Integer quantityInStock
) {}
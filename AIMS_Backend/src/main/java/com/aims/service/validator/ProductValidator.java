/**
 * SRP VIOLATION:
 * This class has two responsibilities:
 * (1) Validate common fields (price, barcode, title) via validateCommonFields()
 * (2) Declare the type-specific validation template via abstract validateTypeFields()
 *
 * Impact: A change to the common pricing business rule forces modification of the
 * base class, risking unintended impact on all subtype validators.
 *
 * Improvement: Extract a standalone ProductCommonValidator class responsible only
 * for common field validation. The abstract ProductValidator delegates to it.
 * Each subtype validator then focuses solely on type-specific rules.
 */

package com.aims.service.validator;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;

public abstract class ProductValidator {
    public final void validate(ProductInfoDTO dto) {
        validateCommonFields(dto);
        validateTypeFields(dto);
    }

    private void validateCommonFields(ProductInfoDTO productInfo) {
        if (productInfo.getTitle() == null || productInfo.getTitle().isBlank()) {
            throw new InvalidProductInfoException("Title must not be empty");
        }
        if (productInfo.getBarcode() == null || productInfo.getBarcode().isBlank()) {
            throw new InvalidProductInfoException("Barcode must not be empty");
        }
        if (productInfo.getCategory() == null || productInfo.getCategory().isBlank()) {
            throw new InvalidProductInfoException("Category must not be empty");
        }
        if (productInfo.getImage() == null || productInfo.getImage().isBlank()) {
            throw new InvalidProductInfoException("Image must not be empty");
        }
        if (productInfo.getDescription() == null || productInfo.getDescription().isBlank()) {
            throw new InvalidProductInfoException("Description must not be empty");
        }
        if (productInfo.getDimensions() == null || productInfo.getDimensions().isBlank()) {
            throw new InvalidProductInfoException("Dimensions must not be empty");
        }
        if (productInfo.getOriginalValue() == null || productInfo.getOriginalValue() <= 0) {
            throw new InvalidProductInfoException("Original value must be positive");
        }
        if (productInfo.getSellingPrice() == null || productInfo.getSellingPrice() <= 0) {
            throw new InvalidProductInfoException("Selling price must be positive");
        }
        if (productInfo.getSellingPrice() < productInfo.getOriginalValue() * 0.3) {
            throw new InvalidProductInfoException(
                    "Selling price must not smaller than 30% of original value");
        }
        if (productInfo.getSellingPrice() > productInfo.getOriginalValue() * 1.5) {
            throw new InvalidProductInfoException(
                    "Selling price must not exceed 150% of original value");
        }
        if (productInfo.getWeight() == null || productInfo.getWeight() <= 0) {
            throw new InvalidProductInfoException("Weight must be positive");
        }
        if (productInfo.getProductType() == null) {
            throw new InvalidProductInfoException("Product type is required");
        }
    }

    protected abstract void validateTypeFields(ProductInfoDTO dto);
}
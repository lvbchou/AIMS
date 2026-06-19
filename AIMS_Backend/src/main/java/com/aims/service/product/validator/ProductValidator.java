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

package com.aims.service.product.validator;

import com.aims.dto.product.ProductInfoDTO;

public abstract class ProductValidator<T extends ProductInfoDTO> {

    private final ProductCommonValidator commonValidator;

    protected ProductValidator(ProductCommonValidator commonValidator) {
        this.commonValidator = commonValidator;
    }
    // Delegate common validation
    public void validate(T dto) {
        commonValidator.validate(dto);
        validateTypeFields(dto);
    }

    // Subclasses only implement type-specific rules
    protected abstract void validateTypeFields(T dto);
    public abstract String getSupportedType();
}
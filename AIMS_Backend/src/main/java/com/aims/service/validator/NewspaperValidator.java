/**
 * LSP VIOLATION:
 * validateTypeFields(ProductInfoDTO dto) declares it accepts any ProductInfoDTO
 * (the parent type), but immediately downcasts to a specific subtype
 * (e.g., BookInfoDTO book = (BookInfoDTO) dto).
 *
 * Impact: If a caller substitutes a different ProductInfoDTO subtype, a
 * ClassCastException is thrown at runtime. The subclass cannot safely substitute
 * the base ProductValidator for all valid inputs of the declared type.
 *
 * Improvement: Use generics — abstract class ProductValidator<T extends ProductInfoDTO>
 * with abstract void validateTypeFields(T dto). Each subtype validator is typed as
 * ProductValidator<BookInfoDTO>, ProductValidator<CDInfoDTO>, etc.
 * Type correctness is enforced at compile time; no runtime downcast is needed.
 */
package com.aims.service.validator;

import com.aims.dto.product.NewspaperInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

@Component
public class NewspaperValidator extends ProductValidator {

    @Override
    protected void validateTypeFields(ProductInfoDTO dto) {
        NewspaperInfoDTO newspaper = (NewspaperInfoDTO) dto;

        if (newspaper.getEditorInChief() == null || newspaper.getEditorInChief().isBlank())
            throw new InvalidProductInfoException("Editor in chief is required for Newspaper");

        if (newspaper.getPublisher() == null || newspaper.getPublisher().isBlank())
            throw new InvalidProductInfoException("Publisher is required for Newspaper");

        if (newspaper.getPublicationDate() == null)
            throw new InvalidProductInfoException("Publication date is required for Newspaper");
    }
}
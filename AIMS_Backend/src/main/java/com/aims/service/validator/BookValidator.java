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

import com.aims.dto.product.BookInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

@Component
public class BookValidator extends ProductValidator {

    @Override
    protected void validateTypeFields(ProductInfoDTO dto) {
        BookInfoDTO book = (BookInfoDTO) dto;

        if (book.getAuthor() == null || book.getAuthor().isBlank())
            throw new InvalidProductInfoException("Author is required for Book");

        if (book.getPublisher() == null || book.getPublisher().isBlank())
            throw new InvalidProductInfoException("Publisher is required for Book");

        if (book.getPublicationDate() == null)
            throw new InvalidProductInfoException("Publication date is required for Book");

        String cover = book.getCoverType();
        if (cover == null || cover.isBlank())
            throw new InvalidProductInfoException("Cover type is required for Book");
        if (!cover.equalsIgnoreCase("HARDCOVER") && !cover.equalsIgnoreCase("PAPERBACK"))
            throw new InvalidProductInfoException("Cover type must be Paperback or Hardcover");

        if (book.getPages() != null && book.getPages() <= 0)
            throw new InvalidProductInfoException("Pages must be positive");
    }
}
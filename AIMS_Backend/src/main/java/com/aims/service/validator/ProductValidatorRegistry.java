/**
 * OCP VIOLATION:
 * The constructor hardcodes exactly four product types using
 * Map.of("BOOK", bookValidator, "CD", ...).
 * Adding a new product type (e.g., VINYL) requires modifying this constructor
 * directly — a violation of closed-for-modification.
 *
 * Impact: Every new product type forces modification of this registry class,
 * increasing risk of regression in existing validator mappings.
 *
 * Improvement: Have each ProductValidator implement a getSupportedType() method.
 * Inject List<ProductValidator> via Spring; the registry builds the map
 * automatically from the list. A new product type only requires adding a new
 * @Component — no registry class is modified.
 */

package com.aims.service.validator;

import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProductValidatorRegistry {

    private final Map<String, ProductValidator> validators;

    public ProductValidatorRegistry(
            BookValidator bookValidator,
            CDValidator cdValidator,
            DVDValidator dvdValidator,
            NewspaperValidator newspaperValidator) {

        this.validators = Map.of(
                "BOOK",      bookValidator,
                "CD",        cdValidator,
                "DVD",       dvdValidator,
                "NEWSPAPER", newspaperValidator
        );
    }

    public ProductValidator getValidator(String productType) {
        ProductValidator validator = validators.get(productType.toUpperCase());
        if (validator == null)
            throw new InvalidProductInfoException(
                    "Unknown product type: " + productType);
        return validator;
    }
}
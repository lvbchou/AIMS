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

package com.aims.service.product.validator;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductValidatorRegistry {

    // Dùng wildcard
    private final Map<String, ProductValidator<?>> validators;

    public ProductValidatorRegistry(List<ProductValidator<?>> validatorList) {
        this.validators = validatorList.stream()
                .collect(Collectors.toMap(
                        ProductValidator::getSupportedType,
                        v -> v
                ));
    }

    @SuppressWarnings("unchecked")
    public <T extends ProductInfoDTO> ProductValidator<T> getValidator(String type) {
        ProductValidator<?> validator = validators.get(type.toUpperCase());
        if (validator == null)
            throw new InvalidProductInfoException("Unknown product type: " + type);
        return (ProductValidator<T>) validator;
    }
}
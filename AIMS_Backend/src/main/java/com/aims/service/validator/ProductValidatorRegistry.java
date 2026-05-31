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
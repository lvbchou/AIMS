/**
 * OCP VIOLATION:
 * Same issue as ProductValidatorRegistry: the constructor hardcodes four
 * product types. Any new product type forces modification of this class.
 *
 * Impact: Every new product type forces modification of this registry class,
 * increasing risk of regression in existing creator mappings.
 *
 * Improvement: Inject List<ProductCreator> via Spring; each creator declares
 * its supported type via getSupportedType(). The registry self-assembles the
 * map without any manual modification.
 */

package com.aims.service.creator;

import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProductCreatorRegistry {

    private final Map<String, ProductCreator> factories;

    public ProductCreatorRegistry(
            BookCreator bookFactory,
            CDCreator cdFactory,
            DVDCreator dvdFactory,
            NewspaperCreator newspaperFactory) {

        this.factories = Map.of(
                "BOOK",      bookFactory,
                "CD",        cdFactory,
                "DVD",       dvdFactory,
                "NEWSPAPER", newspaperFactory
        );
    }

    public ProductCreator getFactory(String productType) {
        ProductCreator factory = factories.get(productType.toUpperCase());
        if (factory == null)
            throw new InvalidProductInfoException(
                    "Unknown product type: " + productType);
        return factory;
    }
}
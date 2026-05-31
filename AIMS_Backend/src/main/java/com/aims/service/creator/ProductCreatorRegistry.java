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
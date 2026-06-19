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

package com.aims.service.product.creator;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductCreatorRegistry {

    private final Map<String, ProductCreator<?>> creators;

    public ProductCreatorRegistry(List<ProductCreator<?>> creatorList) {
        this.creators = creatorList.stream()
                .collect(Collectors.toMap(
                        ProductCreator::getSupportedType,
                        c -> c
                ));
    }

    @SuppressWarnings("unchecked")
    public <T extends ProductInfoDTO> ProductCreator<T> getCreator(String productType) {
        ProductCreator<?> creator = creators.get(productType.toUpperCase());
        if (creator == null) {
            throw new InvalidProductInfoException("Unknown product type: " + productType);
        }
        return (ProductCreator<T>) creator;
    }
}
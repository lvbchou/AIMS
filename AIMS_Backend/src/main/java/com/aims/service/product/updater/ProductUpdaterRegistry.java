package com.aims.service.product.updater;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductUpdaterRegistry {
    private final Map<String, ProductUpdater<?, ?>> updaters;

    public ProductUpdaterRegistry(List<ProductUpdater<?, ?>> updaterList) {
        this.updaters = updaterList.stream()
                .collect(Collectors.toMap(
                        ProductUpdater::getSupportedType,
                        u -> u
                ));
    }

    @SuppressWarnings("unchecked")
    public <P extends Product, T extends ProductInfoDTO> ProductUpdater<P, T> getUpdater(String type) {
        ProductUpdater<?, ?> updater = updaters.get(type.toUpperCase());
        if (updater == null)
            throw new InvalidProductInfoException("Unknown product type: " + type);
        return (ProductUpdater<P, T>) updater;
    }
}

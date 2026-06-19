package com.aims.mapper.product;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;
import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductMapperRegistry {
    private final Map<Class<? extends Product>, ProductMapper<?, ?>> mappers;

    public ProductMapperRegistry(List<ProductMapper<?, ?>> mapperList) {
        this.mappers = mapperList.stream()
                .collect(Collectors.toMap(
                        ProductMapper::supportedType,
                        m -> m
                ));
    }

    @SuppressWarnings("unchecked")
    public <T extends ProductInfoDTO, P extends Product> ProductMapper<T, P> getMapper(P product){
        ProductMapper<?, ?> mapper = mappers.get(product.getClass());
        if(mapper == null){
            throw new InvalidProductInfoException("Unknown product type: " + product.getClass().getSimpleName());
        }
        return (ProductMapper<T, P>) mapper;
    }
}

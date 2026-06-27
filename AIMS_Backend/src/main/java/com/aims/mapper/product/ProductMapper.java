package com.aims.mapper.product;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;

public abstract class ProductMapper<T extends ProductInfoDTO, P extends Product> {

    private final ProductCommonMapper commonMapper;

    protected ProductMapper(ProductCommonMapper commonMapper){
        this.commonMapper = commonMapper;
    }

    public T toDTO(P product){
        T dto = createDTO();
        commonMapper.mapCommon(dto, product);
        mapTypeFields(dto, product);
        return dto;
    }

    public abstract Class<P> supportedType();
    protected abstract void mapTypeFields(T dto, P product);
    protected abstract T createDTO();
}

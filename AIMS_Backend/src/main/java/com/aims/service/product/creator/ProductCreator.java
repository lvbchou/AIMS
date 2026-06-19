package com.aims.service.product.creator;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;

public abstract class ProductCreator<T extends ProductInfoDTO> {

    // Template method — lock thứ tự
    public final Product create(T dto) {
        Product product = buildProduct(dto);
        product.setStatus("active");
        return product;
    }

    // Subclass implement phần riêng
    protected abstract Product buildProduct(T dto);
    public abstract String getSupportedType();
}
package com.aims.service.creator;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;

public abstract class ProductCreator {

    // Template method — lock thứ tự
    public final Product create(ProductInfoDTO dto) {
        Product product = buildProduct(dto);
        product.setStatus("active");
        return product;
    }

    // Subclass implement phần riêng
    protected abstract Product buildProduct(ProductInfoDTO dto);
}
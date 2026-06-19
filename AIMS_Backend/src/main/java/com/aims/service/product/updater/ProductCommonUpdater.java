package com.aims.service.product.updater;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductCommonUpdater {
    public void update(Product product, ProductInfoDTO dto){
        product.setTitle(dto.getTitle());
        product.setCategory(dto.getCategory());
        product.setBarcode(dto.getBarcode());
        product.setImage(dto.getImage());
        product.setStatus("active");
        product.setOriginalValue(dto.getOriginalValue());
        product.setSellingPrice(dto.getSellingPrice());
        product.setWeight(dto.getWeight());
        product.setDescription(dto.getDescription());
        product.setDimensions(dto.getDimensions());
    }
}

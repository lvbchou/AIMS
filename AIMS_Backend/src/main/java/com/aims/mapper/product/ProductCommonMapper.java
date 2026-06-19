package com.aims.mapper.product;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductCommonMapper {
    public void mapCommon(ProductInfoDTO dto, Product product){
        dto.setProductId(product.getProductId());
        dto.setTitle(product.getTitle());
        dto.setCategory(product.getCategory());
        dto.setBarcode(product.getBarcode());
        dto.setImage(product.getImage());
        dto.setStatus(product.getStatus());
        dto.setOriginalValue(product.getOriginalValue());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setWeight(product.getWeight());
        dto.setDescription(product.getDescription());
        dto.setDimensions(product.getDimensions());
        dto.setQuantityInStock(product.getQuantityInStock());
    }
}

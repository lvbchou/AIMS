package com.aims.mapper.product;

import com.aims.dto.product.ProductSummaryDTO;
import com.aims.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductSummaryMapper {

    public ProductSummaryDTO toDTO(Product product) {
        return new ProductSummaryDTO(
                product.getProductId(),
                product.getTitle(),
                product.getClass().getSimpleName().toUpperCase(),
                product.getSellingPrice(),
                product.getImage(),
                product.getQuantityInStock()
        );
    }
}

// service/command/IProductCommandService.java
package com.aims.service.product;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;

import java.util.List;

public interface IProductCommandService {
    Product saveProduct(ProductInfoDTO dto);
    void updateProduct(Integer productId, ProductInfoDTO dto);
    void deleteProduct(Integer productId);
    void deleteManyProducts(List<Integer> productIds);
}
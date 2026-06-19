package com.aims.service.product;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.dto.product.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductQueryService {

    Page<ProductSummaryDTO> getAllProducts(Pageable pageable);

    List<ProductSummaryDTO> getProductsByIds(List<Integer> ids);

    ProductInfoDTO viewProduct(Integer productId);
}

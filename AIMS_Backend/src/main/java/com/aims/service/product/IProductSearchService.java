package com.aims.service.product;

import com.aims.dto.product.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductSearchService {

    Page<ProductSummaryDTO> searchProduct(
            String keyword,
            String category,
            Pageable pageable
    );

    Page<ProductSummaryDTO> filterProductsByPriceRange(
            String keyword,
            String category,
            String priceRange,
            Pageable pageable
    );
}
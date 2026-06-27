package com.aims.controller.product;

import com.aims.dto.product.ProductSummaryDTO;
import com.aims.service.product.IProductSearchService;
import com.aims.service.product.ProductSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductSearchController {

    private final IProductSearchService productSearchService;

    public ProductSearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductSummaryDTO>> searchAndFilterProduct(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priceRange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (priceRange != null && !priceRange.isBlank()) {
            return ResponseEntity.ok(
                    productSearchService.filterProductsByPriceRange(keyword, category, priceRange, pageable));
        }

        return ResponseEntity.ok(
                productSearchService.searchProduct(keyword, category, pageable));
    }
}

package com.aims.controller.product;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.dto.product.ProductSummaryDTO;
import com.aims.service.product.IProductQueryService;
import com.aims.service.product.ProductQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductQueryController {

    private final IProductQueryService productQueryService;

    public ProductQueryController(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    @GetMapping
    public Page<ProductSummaryDTO> getAllProducts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productQueryService.getAllProducts(pageable);
    }

    @GetMapping("/batch")
    public List<ProductSummaryDTO> getItemByIds(
            @RequestParam List<Integer> ids) {
        return productQueryService.getProductsByIds(ids);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductInfoDTO> viewProduct(@PathVariable Integer productId) {
        ProductInfoDTO productDetails = productQueryService.viewProduct(productId);
        return ResponseEntity.ok(productDetails);
    }
}

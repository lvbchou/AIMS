/**
 * ProductController
 *
 * Cohesion Level: Functional
 * Reason: Each method has a single well-defined purpose —
 *   map one HTTP endpoint to one service call. No business logic present.
 *
 * Coupling:
 *   - Data coupling with ProductService: passes only primitives
 *     or purpose-built DTOs and receives purpose-built response types.
 */
/*
    Coupling level: Control coupling with ProductService (in searchAndFilterProduct()).
        - Reason : The controller checks priceRange != null to decide which service method
                   to call (filterProductsByPriceRange vs searchProduct). The flow of
                   execution is controlled by a flag — this is control coupling.
*/
package com.aims.controller;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.dto.product.ProductSummaryDTO;
import com.aims.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET ALL PRODUCT
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
        return productService.getAllProducts(pageable);
    }
    /**
     * POST /api/products
     * createProduct(product: Product): void
     */
    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody ProductInfoDTO productInfo) {
        productService.saveProduct(productInfo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * PUT /api/products/{productId}
     * updateProduct(product: Product): void
     * Note: re-validates and replaces the product record.
     */
    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Integer productId,
            @RequestBody ProductInfoDTO productInfo) {
        productService.updateProduct(productId, productInfo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMany(@RequestBody List<Integer> productIds) {
        productService.deleteManyProducts(productIds);
        return ResponseEntity.noContent().build();
    }

    // VIEW PRODUCT DETAILS
    @GetMapping("/{productId}")
    public ResponseEntity<ProductInfoDTO> viewProduct(@PathVariable Integer productId) {
        ProductInfoDTO productDetails = productService.viewProduct(productId);
        return ResponseEntity.ok(productDetails);
    }

    // SEARCH AND FILTER PRODUCT
    @GetMapping("/search")
    public ResponseEntity<Page<ProductSummaryDTO>> searchAndFilterProduct(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priceRange,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (priceRange != null && !priceRange.isBlank()) {
            return ResponseEntity.ok(
                    productService.filterProductsByPriceRange(keyword, category, priceRange, pageable));
        }

        return ResponseEntity.ok(
                productService.searchProduct(keyword, category, pageable));
    }
}

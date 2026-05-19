package com.aims.controller;

import com.aims.dto.ProductInfoDTO;
import com.aims.entity.Product;
import com.aims.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController - REST layer matching the ProductController class in the control diagram.
 * Handles: createProduct, updateProduct, deleteProduct, viewProduct, searchProduct, filterProduct
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
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
            @PathVariable int productId,
            @RequestBody ProductInfoDTO productInfo) {
        productService.updateProduct(productId, productInfo);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/products/{productId}
     * deleteProduct(product: Product): void
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        Product product = productService.viewProduct(productId);
        productService.deleteProduct(product);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/products/{barcode}
     * viewProduct(barcode: String): Product
     */
    @GetMapping("/{barcode}")
    public ResponseEntity<Product> viewProduct(@PathVariable String barcode) {
        Product product = productService.viewProduct(barcode);
        return ResponseEntity.ok(product);
    }

    /**
     * GET /api/products/search?keyword=...&category=...
     * searchProduct(keyword: String, category: String): List<Product>
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProduct(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<Product> results = productService.searchProduct(keyword, category);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/products/filter?priceRange=min-max
     * filterProduct(products: List<Product>, priceRange: String): List<Product>
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProduct(
            @RequestParam String priceRange) {
        List<Product> results = productService.filterProduct(null, priceRange);
        return ResponseEntity.ok(results);
    }
}

package com.aims.controller;

import com.aims.dto.ProductInfoDTO;
import com.aims.dto.ProductSummaryDTO;
import com.aims.entity.Product;
import com.aims.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.parser.Entity;
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

    @GetMapping
    public ResponseEntity<List<ProductSummaryDTO>> getAllProducts() {
        List<ProductSummaryDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    /**
     * POST /api/products
     * createProduct(product: Product): void
    /**
     * POST /api/products
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
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) {
        productService.deleteProduct(productId);
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
    @GetMapping("/{productId}")
    public ResponseEntity<ProductInfoDTO> viewProduct(@PathVariable Integer productId) {
        ProductInfoDTO product = productService.viewProduct(productId);
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
    public ResponseEntity<List<ProductInfoDTO>> searchProduct(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priceRange) {

        // 1. Search trước
        List<ProductInfoDTO> results = productService.searchProduct(keyword, category);

        // 2. Filter nếu có priceRange
        if (priceRange != null && !priceRange.isBlank()) {
            results = productService.filterProduct(results, priceRange);
        }

        return ResponseEntity.ok(results);
    }
}
     * GET /api/products/search?keyword=...
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProduct(
            @RequestParam(required = false) String keyword) {
        List<Product> results = productService.searchProduct(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/products/filter?priceRange=min-max
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProduct(
            @RequestParam String priceRange) {
        List<Product> results = productService.filterProduct(null, priceRange);
        return ResponseEntity.ok(results);
    }
}

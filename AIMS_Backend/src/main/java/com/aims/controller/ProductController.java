package com.aims.controller;

import com.aims.dto.ProductInfoDTO;
import com.aims.dto.ProductSummaryDTO;
import com.aims.service.ProductService;
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
    public ResponseEntity<List<ProductSummaryDTO>> getAllProducts() {
        List<ProductSummaryDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    //CUD PRODUCT
    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody ProductInfoDTO productInfo) {
        productService.saveProduct(productInfo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

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

    //VIEW PRODUCT DETAILS
    @GetMapping("/{productId}")
    public ResponseEntity<ProductInfoDTO> viewProduct(@PathVariable Integer productId) {
        ProductInfoDTO productDetails = productService.viewProduct(productId);
        return ResponseEntity.ok(productDetails);
    }

    // SEARCH AND FILTER PRODUCT
    @GetMapping("/search")
    public ResponseEntity<List<ProductSummaryDTO>> searchAndFilterProduct(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priceRange) {

        if (priceRange != null && !priceRange.isBlank()) {
            List<ProductSummaryDTO> filtered =
                    productService.filterProductsByPriceRange(keyword, category, priceRange);
            return ResponseEntity.ok(filtered);
        }

        List<ProductSummaryDTO> results = productService.searchProduct(keyword, category);
        return ResponseEntity.ok(results);
    }
}
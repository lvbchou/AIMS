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

    /* CUD PRODUCTS */
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


    /* VIEW PRODUCT DETAILS */
     @GetMapping("/{productId}")
    public ResponseEntity<ProductInfoDTO> viewProduct(@PathVariable Integer productId) {
        ProductInfoDTO productDetails = productService.viewProduct(productId);
        return ResponseEntity.ok(productDetails);
    }

    /* SEARCH AND FILTER PRODUCTS */
    @GetMapping("/search")
    public ResponseEntity<List<ProductSummaryDTO>> searchProduct(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
 
        List<ProductSummaryDTO> productList = productService.searchProduct(keyword, category);
        return ResponseEntity.ok(productList);
    }
 

    @GetMapping("/filter")
    public ResponseEntity<List<ProductSummaryDTO>> filterByPriceRange(
            @RequestParam String priceRange) {
        return ResponseEntity.ok(productService.filterByPriceRange(priceRange));
    }
}
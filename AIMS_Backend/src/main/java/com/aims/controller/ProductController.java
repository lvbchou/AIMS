package com.aims.controller;

import com.aims.dto.ProductInfoDTO;
import com.aims.dto.ProductSummaryDTO;
import com.aims.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController - REST layer matching the ProductController class in the control diagram.
 * Handles: createProduct, updateProduct, deleteProduct, viewProduct, searchProduct, filterProduct
 *
 * Coupling  : Data coupling với ProductService — chỉ truyền primitive/DTO, không dùng entity trực tiếp.
 * Cohesion  : Communicational — tất cả endpoint đều thao tác trên cùng resource /api/products.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /* ----------------------------------------------------------------
     * GET ALL PRODUCTS
     * ---------------------------------------------------------------- */
    @GetMapping
    public ResponseEntity<List<ProductSummaryDTO>> getAllProducts() {
        List<ProductSummaryDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /* ----------------------------------------------------------------
     * CUD PRODUCTS
     * ---------------------------------------------------------------- */
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

    /* ----------------------------------------------------------------
     * VIEW PRODUCT DETAILS
     * SD ViewProductDetails step 1.1: viewProductDetails(productID)
     * → service gọi retrieveProductInfo(productId) trên Product entity
     * → trả ProductInfoDTO (SD step 1.1.2 displayProductDetails)
     * → nếu không tìm thấy → GlobalExceptionHandler trả 404
     *   (SD step 1.1.3 displayErrorNotification)
     * ---------------------------------------------------------------- */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductInfoDTO> viewProduct(@PathVariable Integer productId) {
        ProductInfoDTO productDetails = productService.viewProduct(productId);
        return ResponseEntity.ok(productDetails);
    }

    /* ----------------------------------------------------------------
     * SEARCH AND FILTER PRODUCTS
     *
     * SD SearchProduct:
     *   Step 1.1  : searchProduct(keyword, category)
     *   Step 1.1.1: isValidInput → nếu cả hai rỗng → 400
     *               (SD step 1.1.2 displayEmptyInputNotification)
     *   Step 1.1.3: query Product entity → trả productList
     *   Step 1.1.4: nếu list rỗng → frontend hiển thị "No product found"
     *
     *   Step 2.1 (opt): filterProductsByPriceRange(productList, priceRange)
     *               → filter áp dụng TRÊN tập search result, không phải toàn catalog
     *               → truyền keyword + category + priceRange cùng lúc
     *
     * Cả search và filter dùng chung 1 endpoint GET /search.
     * ---------------------------------------------------------------- */

    /**
     * GET /api/products/search
     *
     * @param keyword    tên sản phẩm (optional)
     * @param category   loại sản phẩm (optional)
     * @param priceRange khoảng giá format "min-max", vd "100000-200000" (optional)
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductSummaryDTO>> searchAndFilterProduct(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priceRange) {

        // Có priceRange → SD step 2.1: filter trên search result (keyword + category + priceRange)
        if (priceRange != null && !priceRange.isBlank()) {
            List<ProductSummaryDTO> filtered =
                    productService.filterProductsByPriceRange(keyword, category, priceRange);
            return ResponseEntity.ok(filtered);
        }

        // Không có priceRange → SD step 1.1.3: search thuần
        // isValidInput() được gọi trong service, throw EmptySearchInputException nếu cả hai rỗng
        List<ProductSummaryDTO> results = productService.searchProduct(keyword, category);
        return ResponseEntity.ok(results);
    }
}
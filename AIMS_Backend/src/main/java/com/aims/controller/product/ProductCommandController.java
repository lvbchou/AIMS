package com.aims.controller.product;

import com.aims.dto.ApiResponse;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.service.product.IProductCommandService;
import com.aims.service.product.ProductCommandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductCommandController {

    private final IProductCommandService commandService;

    public ProductCommandController(IProductCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createProduct(@RequestBody ProductInfoDTO productInfo) {
        commandService.saveProduct(productInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "Product created successfully",
                null
        ));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable Integer productId,
            @RequestBody ProductInfoDTO productInfo) {
        commandService.updateProduct(productId, productInfo);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Product updated successfully",
                null
        ));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Integer productId) {
        commandService.deleteProduct(productId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Product deleted successfully",
                null
        ));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteMany(@RequestBody List<Integer> productIds) {
        commandService.deleteManyProducts(productIds);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Product deleted successfully",
                null
        ));
    }
}

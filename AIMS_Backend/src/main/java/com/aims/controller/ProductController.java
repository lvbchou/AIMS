package com.aims.controller;
import com.aims.dto.ProductInfoDTO; import com.aims.entity.Product;
import com.aims.service.ProductService;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService){ this.productService=productService; }
    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody ProductInfoDTO productInfo){
        productService.saveProduct(productInfo); return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(@PathVariable int productId,@RequestBody ProductInfoDTO productInfo){
        productService.updateProduct(productId,productInfo); return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId){
        Product product=productService.viewProduct(productId);
        productService.deleteProduct(product); return ResponseEntity.noContent().build();
    }
    @GetMapping("/{barcode}")
    public ResponseEntity<Product> viewProduct(@PathVariable String barcode){
        return ResponseEntity.ok(productService.viewProduct(barcode));
    }
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProduct(
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) String category){
        return ResponseEntity.ok(productService.searchProduct(keyword,category));
    }
    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProduct(@RequestParam String priceRange){
        return ResponseEntity.ok(productService.filterProduct(null,priceRange));
    }
}

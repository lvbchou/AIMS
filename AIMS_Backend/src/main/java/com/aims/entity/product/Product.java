/**
 * Product
 *
 * Cohesion Level: Functional
 * Reason: All fields contribute to representing a single domain concept — a product entity.
 *
 * Coupling:
 *   - No direct coupling with other classes.
 */
package com.aims.entity.product;

import com.aims.dto.product.ProductInfoDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "category")
    private String category;

    @Column(name = "barcode", unique = true)
    private String barcode;

    @Column(name = "image")
    private String image;

    @Column(name = "original_value")
    private Long originalValue;

    @Column(name = "selling_price")
    private Long sellingPrice;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "quantity")
    private Integer quantityInStock;

    public Product(String title, String category, String barcode, String image,
                   Long originalValue, Long sellingPrice, Double weight,
                   String description, String dimensions, Integer quantityInStock) {
        this.title = title;
        this.category = category;
        this.barcode = barcode;
        this.image = image;
        this.originalValue = originalValue;
        this.sellingPrice = sellingPrice;
        this.weight = weight;
        this.description = description;
        this.dimensions = dimensions;
        this.quantityInStock = quantityInStock;
        this.status = "active";
    }

    public void changeQuantity(int amount) {
        this.quantityInStock += amount;
    }

    public void applyCommonUpdate(ProductInfoDTO dto) {
        this.title = dto.getTitle();
        this.category = dto.getCategory();
        this.barcode = dto.getBarcode();
        this.image = dto.getImage();
        this.status = "active";
        this.originalValue = dto.getOriginalValue();
        this.sellingPrice = dto.getSellingPrice();
        this.weight = dto.getWeight();
        this.description = dto.getDescription();
        this.dimensions = dto.getDimensions();
    }

    protected void baseDTO(ProductInfoDTO dto) {
        dto.setProductId(productId);
        dto.setTitle(title);
        dto.setCategory(category);
        dto.setBarcode(barcode);
        dto.setImage(image);
        dto.setStatus(status);
        dto.setOriginalValue(originalValue);
        dto.setSellingPrice(sellingPrice);
        dto.setWeight(weight);
        dto.setDescription(description);
        dto.setDimensions(dimensions);
        dto.setQuantityInStock(quantityInStock);
    }

    public abstract ProductInfoDTO toDTO();

    public abstract void applyUpdate(ProductInfoDTO dto);
}
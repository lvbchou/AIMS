package com.aims.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int productId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "category")
    private String category;

    @Column(name = "barcode", unique = true)
    private String barcode;

    @Column(name = "image")
    private String image;

    @Column(name = "original_value")
    private long originalValue;

    @Column(name = "selling_price")
    private long sellingPrice;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "weight")
    private double weight;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "quantity")
    private int quantityInStock;

    public Product(String title, String category, String barcode, String image,
                   long originalValue, long sellingPrice, double weight,
                   String description, String dimensions, int quantityInStock) {
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
}

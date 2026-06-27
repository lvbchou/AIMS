package com.aims.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private Integer productId;
    private String title;
    private String category;
    private String barcode;
    private String image;
    private String status;
    private Long originalValue;
    private Long sellingPrice;
    private Integer quantity;
    private Double weight;
    private String dimensions;
    private String description;
}

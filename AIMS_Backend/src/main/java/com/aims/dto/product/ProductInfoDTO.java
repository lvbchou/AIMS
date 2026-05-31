/**
 * ProductInfoDTO
 *
 * Cohesion Level: Logical
 * Reason: Contains fields for four completely unrelated product subtypes
 *   (Book, CD, DVD, Newspaper) in a single flat class. Fields are grouped
 *   only because they are all "product info", not because they belong together.
 *
 * Coupling:
 *   - Control coupling with ProductService: the productType string field
 *     acts as a flag that controls which branch of logic executes
 *     in validateProductInfo() and buildProductFromDTO().
 *
 * Improvement: Split into BookInfoDTO, CDInfoDTO, DVDInfoDTO, NewspaperInfoDTO
 *   extending a common ProductInfoDTO base.
 */
package com.aims.dto.product;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "productType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BookInfoDTO.class,      name = "BOOK"),
        @JsonSubTypes.Type(value = CDInfoDTO.class,        name = "CD"),
        @JsonSubTypes.Type(value = DVDInfoDTO.class,       name = "DVD"),
        @JsonSubTypes.Type(value = NewspaperInfoDTO.class, name = "NEWSPAPER"),
})
public abstract class ProductInfoDTO {

    private Integer productId;
    private String productType;
    private String title;
    private String category;
    private String barcode;
    private String image;
    private String status;
    private Long originalValue;
    private Long sellingPrice;
    private Double weight;
    private String description;
    private String dimensions;
    private Integer quantityInStock;
}
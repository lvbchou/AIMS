/**
 * LSP VIOLATION (applyUpdate method):
 * applyUpdate(ProductInfoDTO dto) accepts the abstract parent type but
 * immediately casts to a concrete subtype
 * (e.g., BookInfoDTO book = (BookInfoDTO) dto).
 *
 * Impact: Passing the wrong DTO subtype causes a ClassCastException at runtime.
 * A caller holding a Product reference cannot safely invoke applyUpdate()
 * with any arbitrary ProductInfoDTO.
 *
 * Improvement: Apply the same generic solution as validators:
 * abstract class Product<T extends ProductInfoDTO> with abstract void applyUpdate(T dto).
 * Each entity subtype declares its own T. Alternatively, add an explicit type-check
 * that throws a descriptive IllegalArgumentException instead of an opaque ClassCastException.
 */
package com.aims.entity.product;

import com.aims.dto.product.DVDInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dvd")
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@Setter
@NoArgsConstructor
public class DVD extends DiscProduct {

    @Column(name = "disc_type")
    private String discType;

    @Column(name = "director")
    private String director;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "studio")
    private String studio;

    @Column(name = "language")
    private String language;

    @Column(name = "subtitles")
    private String subtitles;

    public DVD(String title, String category, String barcode, String image,
               Long originalValue, Long sellingPrice, Double weight,
               String description, String dimensions, Integer quantityInStock,
               String genre, LocalDate releaseDate,
               String discType, String director, Integer runtime,
               String studio, String language, String subtitles) {
        super(title, category, barcode, image, originalValue, sellingPrice,
              weight, description, dimensions, quantityInStock, genre, releaseDate);
        this.discType = discType;
        this.director = director;
        this.runtime = runtime;
        this.studio = studio;
        this.language = language;
        this.subtitles = subtitles;
    }
}
/**
 * DiscProduct
 *
 * Cohesion Level: Functional
 * Reason: All fields contribute to representing a single domain concept —
 *   PrintableProduct represents a printable product (publisher, publicationDate, language);
 *   DiscProduct represents a disc media product (genre, releaseDate).
 *
 * Coupling:
 *   - No direct coupling with other classes.
 */
package com.aims.entity.product;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class DiscProduct extends Product {

    @Column(name = "genre")
    private String genre;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    public DiscProduct(String title, String category, String barcode, String image,
                       long originalValue, long sellingPrice, double weight,
                       String description, String dimensions, int quantityInStock,
                       String genre, LocalDate releaseDate) {
        super(title, category, barcode, image, originalValue, sellingPrice,
              weight, description, dimensions, quantityInStock);
        this.genre = genre;
        this.releaseDate = releaseDate;
    }
}

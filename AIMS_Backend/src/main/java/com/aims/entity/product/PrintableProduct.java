/**
 * PrintableProduct
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
public abstract class PrintableProduct extends Product {

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "language")
    private String language;

    public PrintableProduct(String title, String category, String barcode, String image,
                            Long originalValue, Long sellingPrice, Double weight,
                            String description, String dimensions, Integer quantityInStock,
                            String publisher, LocalDate publicationDate, String language) {
        super(title, category, barcode, image, originalValue, sellingPrice,
              weight, description, dimensions, quantityInStock);
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.language = language;
    }
}
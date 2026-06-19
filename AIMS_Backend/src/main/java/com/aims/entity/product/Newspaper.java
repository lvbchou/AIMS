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

import com.aims.dto.product.NewspaperInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "newspaper")
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@Setter
@NoArgsConstructor
public class  Newspaper extends PrintableProduct {

    @Column(name = "editor_in_chief")
    private String editorInChief;

    @Column(name = "issue_number")
    private String issueNumber;

    @Column(name = "publication_frequency")
    private String publicationFrequency;

    @Column(name = "issn")
    private String ISSN;

    @ElementCollection
    @CollectionTable(
            name = "newspaper_section",
            schema = "aims",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "section_name")
    private List<String> sections = new ArrayList<>();

    public Newspaper(String title, String category, String barcode, String image,
                     Long originalValue, Long sellingPrice, Double weight,
                     String description, String dimensions, Integer quantityInStock,
                     String publisher, LocalDate publicationDate, String language,
                     String editorInChief, String issueNumber, String publicationFrequency,
                     String ISSN, List<String> sections) {
        super(title, category, barcode, image, originalValue, sellingPrice,
              weight, description, dimensions, quantityInStock,
              publisher, publicationDate, language);
        this.editorInChief = editorInChief;
        this.issueNumber = issueNumber;
        this.publicationFrequency = publicationFrequency;
        this.ISSN = ISSN;
        this.sections = sections != null ? new ArrayList<>(sections) : new ArrayList<>();
    }
}
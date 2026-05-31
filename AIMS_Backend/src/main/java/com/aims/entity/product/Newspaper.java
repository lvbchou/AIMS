/**
 * Newspaper
 *
 * Cohesion Level: Functional
 * Reason: All fields contribute to representing a single domain concept — a specific product subtype.
 *
 * Coupling:
 *   - Content coupling with ProductService (updateProduct):
 *     ProductService directly modifies internal data of this class via setters.
 *     Improvement: implement applyUpdate(ProductInfoDTO dto) to encapsulate update logic.
 *   - Content coupling with ProductMapper (toDTO):
 *     ProductMapper directly reads internal data of this class via getters after downcasting.
 *     Improvement: implement toDTO() to encapsulate mapping logic.
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
public class Newspaper extends PrintableProduct {

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

    @Override
    public void applyUpdate(ProductInfoDTO dto) {
        NewspaperInfoDTO newspaper = (NewspaperInfoDTO) dto;
        this.editorInChief = newspaper.getEditorInChief();
        this.issueNumber = newspaper.getIssueNumber();
        this.publicationFrequency = newspaper.getPublicationFrequency();
        this.ISSN = newspaper.getISSN();
        this.setPublisher(newspaper.getPublisher());
        this.setPublicationDate(newspaper.getPublicationDate());
        this.setLanguage(newspaper.getLanguage());

        if (newspaper.getSections() != null) {
            this.sections.clear();
            this.sections.addAll(newspaper.getSections());
        }
    }

    @Override
    public ProductInfoDTO toDTO() {

        NewspaperInfoDTO dto = new NewspaperInfoDTO();

        baseDTO(dto);

        dto.setProductType("NEWSPAPER");
        dto.setPublisher(getPublisher());
        dto.setPublicationDate(getPublicationDate());
        dto.setLanguage(getLanguage());
        dto.setEditorInChief(editorInChief);
        dto.setIssueNumber(issueNumber);
        dto.setPublicationFrequency(publicationFrequency);
        dto.setISSN(ISSN);
        dto.setSections(sections);

        return dto;
    }
}
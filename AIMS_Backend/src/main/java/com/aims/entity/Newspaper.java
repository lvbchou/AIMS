package com.aims.entity;

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
                     long originalValue, long sellingPrice, double weight,
                     String description, String dimensions, int quantityInStock,
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

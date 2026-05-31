package com.aims.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * ProductInfo - form DTO used for Create/Update operations.
 * Maps to the ProductInfo form class in the class diagram.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInfoDTO {

    private String productId;
    private String productType;

    // Common fields
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

    // Book fields
    private String author;
    private String publisher;
    private LocalDate publicationDate;
    private Integer pages;           // ✅ Integer thay vì int
    private String coverType;
    private String language;
    private String genre;

    // Newspaper fields
    private String editorInChief;
    private String issueNumber;
    private String publicationFrequency;
    private String ISSN;
    private List<String> sections;

    // DVD fields
    private String discType;
    private String director;
    private Integer runtime;         // ✅ Integer thay vì int
    private String studio;
    private String subtitles;
    private LocalDate releaseDate;

    // CD fields
    private String recordLabel;
    private List<String> artists;
    private List<TrackDTO> tracks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackDTO {
        private String trackTitle;
        private String trackLength;
    }
}
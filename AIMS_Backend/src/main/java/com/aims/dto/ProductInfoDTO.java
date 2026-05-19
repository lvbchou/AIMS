package com.aims.dto;
import lombok.*; import java.time.LocalDate; import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductInfoDTO {
    private String productId; private String productType;
    private String title; private String category; private String barcode;
    private String image; private String status;
    private Long originalValue; private Long sellingPrice; private Double weight;
    private String description; private String dimensions; private Integer quantityInStock;
    private String author; private String publisher; private LocalDate publicationDate;
    private Integer pages; private String coverType; private String language; private String genre;
    private String editorInChief; private String issueNumber; private String publicationFrequency;
    private String ISSN; private List<String> sections;
    private String discType; private String director; private Integer runtime;
    private String studio; private String subtitles; private LocalDate releaseDate;
    private String recordLabel; private List<String> artists; private List<TrackDTO> tracks;
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class TrackDTO { private String trackTitle; private String trackLength; }
}

package com.aims.dto.product;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewspaperInfoDTO extends ProductInfoDTO {

    private String publisher;
    private LocalDate publicationDate;
    private String language;
    private String editorInChief;
    private String issueNumber;
    private String publicationFrequency;
    private String ISSN;
    private List<String> sections;
}
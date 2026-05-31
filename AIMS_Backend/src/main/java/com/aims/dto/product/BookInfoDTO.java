package com.aims.dto.product;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookInfoDTO extends ProductInfoDTO {

    private String author;
    private String publisher;
    private LocalDate publicationDate;
    private String language;
    private String coverType;
    private Integer pages;
    private String genre;
}
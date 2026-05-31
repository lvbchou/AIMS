package com.aims.dto.product;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DVDInfoDTO extends ProductInfoDTO {

    private String genre;
    private LocalDate releaseDate;
    private String discType;
    private String director;
    private Integer runtime;
    private String studio;
    private String language;
    private String subtitles;
}
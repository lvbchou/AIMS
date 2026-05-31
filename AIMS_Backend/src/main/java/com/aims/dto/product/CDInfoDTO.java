package com.aims.dto.product;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CDInfoDTO extends ProductInfoDTO {

    private String genre;
    private LocalDate releaseDate;
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
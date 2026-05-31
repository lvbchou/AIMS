/**
 * DVD
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

import com.aims.dto.product.DVDInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dvd")
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@Setter
@NoArgsConstructor
public class DVD extends DiscProduct {

    @Column(name = "disc_type")
    private String discType;

    @Column(name = "director")
    private String director;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "studio")
    private String studio;

    @Column(name = "language")
    private String language;

    @Column(name = "subtitles")
    private String subtitles;

    public DVD(String title, String category, String barcode, String image,
               Long originalValue, Long sellingPrice, Double weight,
               String description, String dimensions, Integer quantityInStock,
               String genre, LocalDate releaseDate,
               String discType, String director, Integer runtime,
               String studio, String language, String subtitles) {
        super(title, category, barcode, image, originalValue, sellingPrice,
              weight, description, dimensions, quantityInStock, genre, releaseDate);
        this.discType = discType;
        this.director = director;
        this.runtime = runtime;
        this.studio = studio;
        this.language = language;
        this.subtitles = subtitles;
    }

    @Override
    public void applyUpdate(ProductInfoDTO dto) {
        DVDInfoDTO dvd = (DVDInfoDTO) dto;
        this.discType = dvd.getDiscType();
        this.director = dvd.getDirector();
        this.runtime = dvd.getRuntime();
        this.studio = dvd.getStudio();
        this.language = dvd.getLanguage();
        this.subtitles = dvd.getSubtitles();
        this.setGenre(dvd.getGenre());
        this.setReleaseDate(dvd.getReleaseDate());
    }

    @Override
    public ProductInfoDTO toDTO() {

        DVDInfoDTO dto = new DVDInfoDTO();

        baseDTO(dto);

        dto.setProductType("DVD");
        dto.setGenre(getGenre());
        dto.setDirector(director);
        dto.setRuntime(runtime);
        dto.setStudio(studio);
        dto.setDiscType(discType);
        dto.setLanguage(language);
        dto.setSubtitles(subtitles);
        dto.setReleaseDate(getReleaseDate());

        return dto;
    }
}
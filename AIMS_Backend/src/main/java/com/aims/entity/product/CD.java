/**
 * CD
 *
 * Cohesion Level: Functional
 * Reason: All fields contribute to representing a single domain concept — a specific product subtype.
 *
 * Coupling:
 *   - Stamp coupling with ProductService (via constructor):
 *     receives full ProductInfoDTO to construct the entity,
 *     though only subtype-specific fields are used.
 *   - Content coupling with ProductService (updateProduct):
 *     ProductService directly modifies internal data of this class via setters.
 *     Improvement: implement applyUpdate(ProductInfoDTO dto) to encapsulate update logic.
 *   - Content coupling with ProductMapper (toDTO):
 *     ProductMapper directly reads internal data of this class via getters after downcasting.
 *     Improvement: implement toDTO() to encapsulate mapping logic.
 */
package com.aims.entity.product;

import com.aims.dto.product.CDInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cd")
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@Setter
@NoArgsConstructor
public class CD extends DiscProduct {

    @ElementCollection
    @CollectionTable(
            name = "cd_artist",
            schema = "aims",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "artist_name")
    private List<String> artists = new ArrayList<>();

    @Column(name = "record_label")
    private String recordLabel;

    @OneToMany(mappedBy = "cd", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Track> tracks = new ArrayList<>();

    public CD(String title, String category, String barcode, String image,
              Long originalValue, Long sellingPrice, Double weight,
              String description, String dimensions, Integer quantityInStock,
              String genre, LocalDate releaseDate,
              List<String> artists, String recordLabel) {
        super(title, category, barcode, image, originalValue, sellingPrice,
              weight, description, dimensions, quantityInStock, genre, releaseDate);
        this.artists = artists != null ? new ArrayList<>(artists) : new ArrayList<>();
        this.recordLabel = recordLabel;
    }

    @Override
    public void applyUpdate(ProductInfoDTO dto) {
        CDInfoDTO cd = (CDInfoDTO) dto;
        this.recordLabel = cd.getRecordLabel();
        this.setGenre(cd.getGenre());
        this.setReleaseDate(cd.getReleaseDate());

        if (cd.getArtists() != null) {
            this.artists.clear();
            this.artists.addAll(cd.getArtists());
        }

        if (cd.getTracks() != null) {
            this.tracks.clear();
            cd.getTracks().forEach(t ->
                    this.tracks.add(new Track(t.getTrackTitle(), t.getTrackLength(), this))
            );
        }
    }

    @Override
    public ProductInfoDTO toDTO() {

        CDInfoDTO dto = new CDInfoDTO();

        baseDTO(dto);

        dto.setProductType("CD");
        dto.setGenre(getGenre());
        dto.setReleaseDate(getReleaseDate());
        dto.setRecordLabel(recordLabel);
        dto.setArtists(artists);

        dto.setTracks(
                tracks.stream()
                        .map(track -> new CDInfoDTO.TrackDTO(
                                track.getTrackTitle(),
                                track.getTrackLength()
                        ))
                        .toList()
        );

        return dto;
    }
}
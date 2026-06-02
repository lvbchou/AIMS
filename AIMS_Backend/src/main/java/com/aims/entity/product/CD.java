/**
 * LSP VIOLATION (applyUpdate method):
 * applyUpdate(ProductInfoDTO dto) accepts the abstract parent type but
 * immediately casts to a concrete subtype
 * (e.g., BookInfoDTO book = (BookInfoDTO) dto).
 *
 * Impact: Passing the wrong DTO subtype causes a ClassCastException at runtime.
 * A caller holding a Product reference cannot safely invoke applyUpdate()
 * with any arbitrary ProductInfoDTO.
 *
 * Improvement: Apply the same generic solution as validators:
 * abstract class Product<T extends ProductInfoDTO> with abstract void applyUpdate(T dto).
 * Each entity subtype declares its own T. Alternatively, add an explicit type-check
 * that throws a descriptive IllegalArgumentException instead of an opaque ClassCastException.
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
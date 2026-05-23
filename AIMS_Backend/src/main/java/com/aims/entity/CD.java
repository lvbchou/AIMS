package com.aims.entity;

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
}
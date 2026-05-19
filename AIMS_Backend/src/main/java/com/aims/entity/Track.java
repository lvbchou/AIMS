package com.aims.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "track")
@Getter
@Setter
@NoArgsConstructor
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_id")
    private int trackId;

    @Column(name = "track_title")
    private String trackTitle;

    @Column(name = "track_length")
    private String trackLength;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private CD cd;

    public Track(String trackTitle, String trackLength, CD cd) {
        this.trackTitle = trackTitle;
        this.trackLength = trackLength;
        this.cd = cd;
    }
}

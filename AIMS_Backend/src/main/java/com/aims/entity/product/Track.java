/**
 * Track
 *
 * Cohesion Level: Functional
 * Reason: All fields contribute to representing a single domain concept — a music track.
 *
 * Coupling:
 *   - Stamp coupling with CD (constructor): receives full CD object
 *     but only uses it to establish the @ManyToOne JPA relationship.
 *     Acceptable — JPA requires an entity reference for relationship mapping.
 *     This is an intentional design choice, not lazy stamp coupling.
 */
package com.aims.entity.product;

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

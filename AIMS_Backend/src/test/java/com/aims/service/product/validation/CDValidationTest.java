package com.aims.service.product.validation;

import com.aims.dto.product.CDInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import com.aims.service.product.validator.CDValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CDValidationTest {

    private CDValidator cdValidator;

    @BeforeEach
    void setUp() {
        cdValidator = new CDValidator();
    }

    // =========================================================
    // FACTORY METHOD
    // =========================================================

    private CDInfoDTO buildValidCDDTO() {

        CDInfoDTO dto = new CDInfoDTO();

        // Common required fields
        dto.setProductType("CD");
        dto.setTitle("Abbey Road");
        dto.setCategory("Music");
        dto.setBarcode("CD-001");
        dto.setImage("abbey_road.png");
        dto.setOriginalValue(120000L);
        dto.setSellingPrice(150000L);
        dto.setWeight(0.1);
        dto.setDescription("Classic Beatles album");
        dto.setDimensions("14x12x1cm");

        // CD-specific required fields
        dto.setArtists(new ArrayList<>(List.of("The Beatles")));
        dto.setRecordLabel("Apple Records");
        dto.setGenre("Rock");

        dto.setTracks(new ArrayList<>(List.of(
                new CDInfoDTO.TrackDTO(
                        "Come Together",
                        "4:19")
        )));

        // Optional fields
        dto.setReleaseDate(null);

        return dto;
    }

    // =========================================================
    // CD VALIDATION TESTS
    // =========================================================

    @Nested
    class CDValidationTests {

        // -----------------------------------------------------
        // HAPPY PATH
        // covers: all required fields valid, optional fields null
        // -----------------------------------------------------

        @Test
        void validCD_shouldNotThrow() {

            CDInfoDTO dto = buildValidCDDTO();

            assertDoesNotThrow(() -> cdValidator.validate(dto));
        }

        @Test
        void validCD_withReleaseDate_shouldNotThrow() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setReleaseDate(LocalDate.of(1969, 9, 26));

            assertDoesNotThrow(() -> cdValidator.validate(dto));
        }

        // -----------------------------------------------------
        // ARTISTS VALIDATION
        // -----------------------------------------------------

        // covers: artists = null
        @Test
        void nullArtists_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setArtists(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Artists is required for CD", ex.getMessage());
        }

        // covers: artists = empty list
        @Test
        void emptyArtists_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setArtists(new ArrayList<>());

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Artists is required for CD", ex.getMessage());
        }

        // -----------------------------------------------------
        // RECORD LABEL VALIDATION
        // -----------------------------------------------------

        // covers: recordLabel = null
        @Test
        void nullRecordLabel_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setRecordLabel(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Record label is required for CD", ex.getMessage());
        }

        // covers: recordLabel = empty string
        @Test
        void emptyRecordLabel_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setRecordLabel("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Record label is required for CD", ex.getMessage());
        }

        // -----------------------------------------------------
        // GENRE VALIDATION
        // -----------------------------------------------------

        // covers: genre = null
        @Test
        void nullGenre_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setGenre(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Genre is required for CD", ex.getMessage());
        }

        // covers: genre = empty string
        @Test
        void emptyGenre_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setGenre("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Genre is required for CD", ex.getMessage());
        }

        // -----------------------------------------------------
        // TRACKS VALIDATION
        // -----------------------------------------------------

        // covers: tracks = null
        @Test
        void nullTracks_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setTracks(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("CD must have at least one track", ex.getMessage());
        }

        // covers: tracks = empty list
        @Test
        void emptyTracks_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setTracks(new ArrayList<>());

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("CD must have at least one track", ex.getMessage());
        }

        // -----------------------------------------------------
        // TRACK TITLE VALIDATION
        // -----------------------------------------------------

        // covers: trackTitle = null
        @Test
        void nullTrackTitle_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setTracks(new ArrayList<>(List.of(
                    new CDInfoDTO.TrackDTO(null, "4:19")
            )));

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Track title is required", ex.getMessage());
        }

        // covers: trackTitle = empty string
        @Test
        void emptyTrackTitle_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setTracks(new ArrayList<>(List.of(
                    new CDInfoDTO.TrackDTO("", "4:19")
            )));

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Track title is required", ex.getMessage());
        }

        // -----------------------------------------------------
        // TRACK LENGTH VALIDATION
        // -----------------------------------------------------

        // covers: trackLength = null
        @Test
        void nullTrackLength_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setTracks(new ArrayList<>(List.of(
                    new CDInfoDTO.TrackDTO(
                            "Come Together",
                            null)
            )));

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Track length is required", ex.getMessage());
        }

        // covers: trackLength = empty string
        @Test
        void emptyTrackLength_shouldThrowException() {

            CDInfoDTO dto = buildValidCDDTO();

            dto.setTracks(new ArrayList<>(List.of(
                    new CDInfoDTO.TrackDTO(
                            "Come Together",
                            "")
            )));

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> cdValidator.validate(dto));

            assertEquals("Track length is required", ex.getMessage());
        }
    }
}
package com.aims.service.product.validation;

import com.aims.dto.product.DVDInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import com.aims.service.product.validator.DVDValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DVDValidationTest {

    private DVDValidator dvdValidator;

    @BeforeEach
    void setUp() {
        dvdValidator = new DVDValidator();
    }

    // =========================================================
    // FACTORY METHOD
    // =========================================================

    private DVDInfoDTO buildValidDVDDTO() {

        DVDInfoDTO dto = new DVDInfoDTO();

        // Common required fields
        dto.setProductType("DVD");
        dto.setTitle("Inception");
        dto.setCategory("Movie");
        dto.setBarcode("DVD-001");
        dto.setImage("inception.png");
        dto.setOriginalValue(150000L);
        dto.setSellingPrice(200000L);
        dto.setWeight(0.1);
        dto.setDescription("A mind-bending thriller");
        dto.setDimensions("19x13x1.5cm");

        // DVD-specific required fields
        dto.setDiscType("Blu-ray");
        dto.setDirector("Christopher Nolan");
        dto.setRuntime(148);
        dto.setStudio("Warner Bros");
        dto.setLanguage("English");
        dto.setSubtitles("Vietnamese");

        // Optional fields
        dto.setReleaseDate(null);
        dto.setGenre(null);

        return dto;
    }

    // =========================================================
    // DVD VALIDATION TESTS
    // =========================================================

    @Nested
    class DVDValidationTests {

        // -----------------------------------------------------
        // HAPPY PATH
        // -----------------------------------------------------

        // covers: all required fields valid, discType = Blu-ray, optional null
        @Test
        void validDVD_BluRay_shouldNotThrow() {

            DVDInfoDTO dto = buildValidDVDDTO();

            assertDoesNotThrow(() -> dvdValidator.validate(dto));
        }

        // covers: optional fields releaseDate and genre provided
        @Test
        void validDVD_withOptionalFields_shouldNotThrow() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setReleaseDate(LocalDate.of(2010, 7, 16));
            dto.setGenre("Sci-Fi");

            assertDoesNotThrow(() -> dvdValidator.validate(dto));
        }

        // covers: discType = HD-DVD (second valid value)
        @Test
        void validDVD_HDDVD_shouldNotThrow() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setDiscType("HD-DVD");

            assertDoesNotThrow(() -> dvdValidator.validate(dto));
        }

        // -----------------------------------------------------
        // DISC TYPE VALIDATION
        // -----------------------------------------------------

        // covers: discType = null
        @Test
        void nullDiscType_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setDiscType(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Disc type is required for DVD", ex.getMessage());
        }

        // covers: discType = empty string
        @Test
        void emptyDiscType_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setDiscType("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Disc type is required for DVD", ex.getMessage());
        }

        // covers: discType = invalid value (not Blu-ray or HD-DVD)
        @Test
        void invalidDiscType_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setDiscType("VCD");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals(
                    "Disc type must be Blu-ray or HD-DVD",
                    ex.getMessage());
        }

        // -----------------------------------------------------
        // DIRECTOR VALIDATION
        // -----------------------------------------------------

        // covers: director = null
        @Test
        void nullDirector_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setDirector(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Director is required for DVD", ex.getMessage());
        }

        // covers: director = empty string
        @Test
        void emptyDirector_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setDirector("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Director is required for DVD", ex.getMessage());
        }

        // -----------------------------------------------------
        // RUNTIME VALIDATION
        // -----------------------------------------------------

        // covers: runtime = null
        @Test
        void nullRuntime_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setRuntime(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Runtime is required for DVD", ex.getMessage());
        }

        // covers: runtime = 0 (lower boundary — invalid)
        @Test
        void zeroRuntime_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setRuntime(0);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Runtime must be positive", ex.getMessage());
        }

        // covers: runtime = negative (invalid)
        @Test
        void negativeRuntime_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setRuntime(-1);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Runtime must be positive", ex.getMessage());
        }

        // covers: runtime = 1 (lower boundary + 1 — valid)
        @Test
        void oneRuntime_shouldNotThrow() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setRuntime(1);

            assertDoesNotThrow(() -> dvdValidator.validate(dto));
        }

        // -----------------------------------------------------
        // STUDIO VALIDATION
        // -----------------------------------------------------

        // covers: studio = null
        @Test
        void nullStudio_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setStudio(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Studio is required for DVD", ex.getMessage());
        }

        // covers: studio = empty string
        @Test
        void emptyStudio_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setStudio("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Studio is required for DVD", ex.getMessage());
        }

        // -----------------------------------------------------
        // LANGUAGE VALIDATION
        // -----------------------------------------------------

        // covers: language = null
        @Test
        void nullLanguage_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setLanguage(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Language is required for DVD", ex.getMessage());
        }

        // covers: language = empty string
        @Test
        void emptyLanguage_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setLanguage("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Language is required for DVD", ex.getMessage());
        }

        // -----------------------------------------------------
        // SUBTITLES VALIDATION
        // -----------------------------------------------------

        // covers: subtitles = null
        @Test
        void nullSubtitles_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setSubtitles(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Subtitles is required for DVD", ex.getMessage());
        }

        // covers: subtitles = empty string
        @Test
        void emptySubtitles_shouldThrowException() {

            DVDInfoDTO dto = buildValidDVDDTO();

            dto.setSubtitles("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> dvdValidator.validate(dto));

            assertEquals("Subtitles is required for DVD", ex.getMessage());
        }
    }
}
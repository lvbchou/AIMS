package com.aims.service.product.validation;

import com.aims.dto.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import com.aims.repository.ProductRepository;
import com.aims.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CDValidationTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository);
    }

    // =========================================================
    // FACTORY METHOD
    // =========================================================

    private ProductInfoDTO buildValidCDDTO() {
        ProductInfoDTO dto = new ProductInfoDTO();

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
                new ProductInfoDTO.TrackDTO("Come Together", "4:19")
        )));

        // Optional fields — null by default
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
        void validCD_shouldReturnTrue() {
            ProductInfoDTO dto = buildValidCDDTO();

            boolean result = productService.validateProductInfo(dto);

            assertTrue(result);
        }

        @Test
        void validCD_withReleaseDate_shouldReturnTrue() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setReleaseDate(java.time.LocalDate.of(1969, 9, 26));

            boolean result = productService.validateProductInfo(dto);

            assertTrue(result);
        }

        // -----------------------------------------------------
        // ARTISTS VALIDATION
        // -----------------------------------------------------

        // covers: artists = null
        @Test
        void nullArtists_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setArtists(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Artists is required for CD", ex.getMessage());
        }

        // covers: artists = empty list
        @Test
        void emptyArtists_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setArtists(new ArrayList<>());

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Artists is required for CD", ex.getMessage());
        }

        // -----------------------------------------------------
        // RECORD LABEL VALIDATION
        // -----------------------------------------------------

        // covers: recordLabel = null
        @Test
        void nullRecordLabel_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setRecordLabel(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Record label is required for CD", ex.getMessage());
        }

        // covers: recordLabel = empty string
        @Test
        void emptyRecordLabel_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setRecordLabel("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Record label is required for CD", ex.getMessage());
        }

        // -----------------------------------------------------
        // GENRE VALIDATION
        // -----------------------------------------------------

        // covers: genre = null
        @Test
        void nullGenre_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setGenre(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Genre is required for CD", ex.getMessage());
        }

        // covers: genre = empty string
        @Test
        void emptyGenre_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setGenre("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Genre is required for CD", ex.getMessage());
        }

        // -----------------------------------------------------
        // TRACKS VALIDATION
        // -----------------------------------------------------

        // covers: tracks = null
        @Test
        void nullTracks_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setTracks(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("CD must have at least one track", ex.getMessage());
        }

        // covers: tracks = empty list
        @Test
        void emptyTracks_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setTracks(new ArrayList<>());

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("CD must have at least one track", ex.getMessage());
        }

        // -----------------------------------------------------
        // TRACK TITLE VALIDATION
        // -----------------------------------------------------

        // covers: trackTitle = null
        @Test
        void nullTrackTitle_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setTracks(new ArrayList<>(List.of(
                    new ProductInfoDTO.TrackDTO(null, "4:19")
            )));

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Track title is required", ex.getMessage());
        }

        // covers: trackTitle = empty string
        @Test
        void emptyTrackTitle_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setTracks(new ArrayList<>(List.of(
                    new ProductInfoDTO.TrackDTO("", "4:19")
            )));

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Track title is required", ex.getMessage());
        }

        // -----------------------------------------------------
        // TRACK LENGTH VALIDATION
        // -----------------------------------------------------

        // covers: trackLength = null
        @Test
        void nullTrackLength_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setTracks(new ArrayList<>(List.of(
                    new ProductInfoDTO.TrackDTO("Come Together", null)
            )));

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Track length is required", ex.getMessage());
        }

        // covers: trackLength = empty string
        @Test
        void emptyTrackLength_shouldThrowException() {
            ProductInfoDTO dto = buildValidCDDTO();
            dto.setTracks(new ArrayList<>(List.of(
                    new ProductInfoDTO.TrackDTO("Come Together", "")
            )));

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Track length is required", ex.getMessage());
        }
    }
}
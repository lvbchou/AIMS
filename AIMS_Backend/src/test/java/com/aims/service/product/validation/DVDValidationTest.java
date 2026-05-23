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

import static org.junit.jupiter.api.Assertions.*;

class DVDValidationTest {

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

    private ProductInfoDTO buildValidDVDDTO() {
        ProductInfoDTO dto = new ProductInfoDTO();

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

        // Optional fields — null by default
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
        void validDVD_BluRay_shouldReturnTrue() {
            ProductInfoDTO dto = buildValidDVDDTO();

            boolean result = productService.validateProductInfo(dto);

            assertTrue(result);
        }

        // covers: optional fields releaseDate and genre provided
        @Test
        void validDVD_withOptionalFields_shouldReturnTrue() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setReleaseDate(java.time.LocalDate.of(2010, 7, 16));
            dto.setGenre("Sci-Fi");

            boolean result = productService.validateProductInfo(dto);

            assertTrue(result);
        }

        // covers: discType = HD-DVD (second valid value)
        @Test
        void validDVD_HDDVD_shouldReturnTrue() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setDiscType("HD-DVD");

            boolean result = productService.validateProductInfo(dto);

            assertTrue(result);
        }

        // -----------------------------------------------------
        // DISC TYPE VALIDATION
        // -----------------------------------------------------

        // covers: discType = null
        @Test
        void nullDiscType_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setDiscType(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Disc type is required for DVD", ex.getMessage());
        }

        // covers: discType = empty string
        @Test
        void emptyDiscType_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setDiscType("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Disc type is required for DVD", ex.getMessage());
        }

        // covers: discType = invalid value (not Blu-ray or HD-DVD)
        @Test
        void invalidDiscType_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setDiscType("VCD");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

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
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setDirector(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Director is required for DVD", ex.getMessage());
        }

        // covers: director = empty string
        @Test
        void emptyDirector_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setDirector("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Director is required for DVD", ex.getMessage());
        }

        // -----------------------------------------------------
        // RUNTIME VALIDATION
        // -----------------------------------------------------

        // covers: runtime = null
        @Test
        void nullRuntime_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setRuntime(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Runtime is required for DVD", ex.getMessage());
        }

        // covers: runtime = 0 (lower boundary — invalid)
        @Test
        void zeroRuntime_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setRuntime(0);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Runtime must be positive", ex.getMessage());
        }

        // covers: runtime = negative (invalid)
        @Test
        void negativeRuntime_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setRuntime(-1);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Runtime must be positive", ex.getMessage());
        }

        // covers: runtime = 1 (lower boundary + 1 — valid)
        @Test
        void oneRuntime_shouldReturnTrue() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setRuntime(1);

            boolean result = productService.validateProductInfo(dto);

            assertTrue(result);
        }

        // -----------------------------------------------------
        // STUDIO VALIDATION
        // -----------------------------------------------------

        // covers: studio = null
        @Test
        void nullStudio_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setStudio(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Studio is required for DVD", ex.getMessage());
        }

        // covers: studio = empty string
        @Test
        void emptyStudio_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setStudio("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Studio is required for DVD", ex.getMessage());
        }

        // -----------------------------------------------------
        // LANGUAGE VALIDATION
        // -----------------------------------------------------

        // covers: language = null
        @Test
        void nullLanguage_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setLanguage(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Language is required for DVD", ex.getMessage());
        }

        // covers: language = empty string
        @Test
        void emptyLanguage_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setLanguage("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Language is required for DVD", ex.getMessage());
        }

        // -----------------------------------------------------
        // SUBTITLES VALIDATION
        // -----------------------------------------------------

        // covers: subtitles = null
        @Test
        void nullSubtitles_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setSubtitles(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Subtitles is required for DVD", ex.getMessage());
        }

        // covers: subtitles = empty string
        @Test
        void emptySubtitles_shouldThrowException() {
            ProductInfoDTO dto = buildValidDVDDTO();
            dto.setSubtitles("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals("Subtitles is required for DVD", ex.getMessage());
        }
    }
}
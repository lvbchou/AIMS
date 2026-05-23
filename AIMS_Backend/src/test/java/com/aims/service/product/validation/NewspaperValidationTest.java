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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class NewspaperValidationTest {

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

    private ProductInfoDTO buildValidNewspaperDTO() {
        ProductInfoDTO dto = new ProductInfoDTO();

        // Common required fields
        dto.setProductType("NEWSPAPER");
        dto.setTitle("Tuoi Tre");
        dto.setCategory("News");
        dto.setBarcode("NEWS-001");
        dto.setImage("tuoitre.png");
        dto.setOriginalValue(15000L);
        dto.setSellingPrice(18000L);
        dto.setWeight(0.2);
        dto.setDescription("Daily newspaper");
        dto.setDimensions("30x21cm");

        // Newspaper-specific required fields
        dto.setEditorInChief("Nguyen Van A");
        dto.setPublisher("Tuoi Tre Publisher");
        dto.setPublicationDate(LocalDate.of(2024, 1, 7));

        // Optional fields — null by default
        dto.setIssueNumber(null);
        dto.setPublicationFrequency(null);
        dto.setISSN(null);
        dto.setLanguage(null);
        dto.setSections(null);

        return dto;
    }

    // =========================================================
    // NEWSPAPER VALIDATION TESTS
    // =========================================================

    @Nested
    class NewspaperValidationTests {

        // -----------------------------------------------------
        // HAPPY PATH
        // covers: all required fields valid, optional fields null
        // -----------------------------------------------------

        @Test
        void validNewspaper_shouldReturnTrue() {
            ProductInfoDTO dto = buildValidNewspaperDTO();

            boolean result = productService.validateProductInfo(dto);

            assertTrue(result);
        }

        // covers: optional fields provided — issueNumber, publicationFrequency, ISSN, language, sections
        @Test
        void validNewspaper_withOptionalFields_shouldReturnTrue() {
            ProductInfoDTO dto = buildValidNewspaperDTO();
            dto.setIssueNumber("42");
            dto.setPublicationFrequency("Weekly");
            dto.setISSN("1234-5678");
            dto.setLanguage("Vietnamese");
            dto.setSections(java.util.List.of("News", "Sports", "Entertainment"));
        }

        // -----------------------------------------------------
        // EDITOR IN CHIEF VALIDATION
        // -----------------------------------------------------

        // covers: editorInChief = null
        @Test
        void nullEditorInChief_shouldThrowException() {
            ProductInfoDTO dto = buildValidNewspaperDTO();
            dto.setEditorInChief(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals(
                    "Editor in chief is required for Newspaper",
                    ex.getMessage());
        }

        // covers: editorInChief = empty string
        @Test
        void emptyEditorInChief_shouldThrowException() {
            ProductInfoDTO dto = buildValidNewspaperDTO();
            dto.setEditorInChief("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals(
                    "Editor in chief is required for Newspaper",
                    ex.getMessage());
        }

        // -----------------------------------------------------
        // PUBLISHER VALIDATION
        // -----------------------------------------------------

        // covers: publisher = null
        @Test
        void nullPublisher_shouldThrowException() {
            ProductInfoDTO dto = buildValidNewspaperDTO();
            dto.setPublisher(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals(
                    "Publisher is required for Newspaper",
                    ex.getMessage());
        }

        // covers: publisher = empty string
        @Test
        void emptyPublisher_shouldThrowException() {
            ProductInfoDTO dto = buildValidNewspaperDTO();
            dto.setPublisher("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals(
                    "Publisher is required for Newspaper",
                    ex.getMessage());
        }

        // -----------------------------------------------------
        // PUBLICATION DATE VALIDATION
        // -----------------------------------------------------

        // covers: publicationDate = null
        @Test
        void nullPublicationDate_shouldThrowException() {
            ProductInfoDTO dto = buildValidNewspaperDTO();
            dto.setPublicationDate(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));

            assertEquals(
                    "Publication date is required for Newspaper",
                    ex.getMessage());
        }
    }
}
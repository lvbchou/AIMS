package com.aims.service.product.validation;

import com.aims.dto.product.NewspaperInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import com.aims.service.product.validator.NewspaperValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NewspaperValidationTest {

    private NewspaperValidator newspaperValidator;

    @BeforeEach
    void setUp() {
        newspaperValidator = new NewspaperValidator();
    }

    // =========================================================
    // FACTORY METHOD
    // =========================================================

    private NewspaperInfoDTO buildValidNewspaperDTO() {

        NewspaperInfoDTO dto = new NewspaperInfoDTO();

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

        // Optional fields
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
        void validNewspaper_shouldNotThrow() {

            NewspaperInfoDTO dto = buildValidNewspaperDTO();

            assertDoesNotThrow(() -> newspaperValidator.validate(dto));
        }

        // covers: optional fields provided
        @Test
        void validNewspaper_withOptionalFields_shouldNotThrow() {

            NewspaperInfoDTO dto = buildValidNewspaperDTO();

            dto.setIssueNumber("42");
            dto.setPublicationFrequency("Weekly");
            dto.setISSN("1234-5678");
            dto.setLanguage("Vietnamese");
            dto.setSections(List.of(
                    "News",
                    "Sports",
                    "Entertainment"));

            assertDoesNotThrow(() -> newspaperValidator.validate(dto));
        }

        // -----------------------------------------------------
        // EDITOR IN CHIEF VALIDATION
        // -----------------------------------------------------

        // covers: editorInChief = null
        @Test
        void nullEditorInChief_shouldThrowException() {

            NewspaperInfoDTO dto = buildValidNewspaperDTO();

            dto.setEditorInChief(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> newspaperValidator.validate(dto));

            assertEquals(
                    "Editor in chief is required for Newspaper",
                    ex.getMessage());
        }

        // covers: editorInChief = empty string
        @Test
        void emptyEditorInChief_shouldThrowException() {

            NewspaperInfoDTO dto = buildValidNewspaperDTO();

            dto.setEditorInChief("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> newspaperValidator.validate(dto));

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

            NewspaperInfoDTO dto = buildValidNewspaperDTO();

            dto.setPublisher(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> newspaperValidator.validate(dto));

            assertEquals(
                    "Publisher is required for Newspaper",
                    ex.getMessage());
        }

        // covers: publisher = empty string
        @Test
        void emptyPublisher_shouldThrowException() {

            NewspaperInfoDTO dto = buildValidNewspaperDTO();

            dto.setPublisher("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> newspaperValidator.validate(dto));

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

            NewspaperInfoDTO dto = buildValidNewspaperDTO();

            dto.setPublicationDate(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> newspaperValidator.validate(dto));

            assertEquals(
                    "Publication date is required for Newspaper",
                    ex.getMessage());
        }
    }
}
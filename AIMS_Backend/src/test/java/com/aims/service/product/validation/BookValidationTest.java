package com.aims.service.product.validation;

import com.aims.dto.product.BookInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import com.aims.service.validator.BookValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BookValidationTest {

    private BookValidator bookValidator;

    @BeforeEach
    void setUp() {
        bookValidator = new BookValidator();
    }

    // =========================================================
    // FACTORY METHOD
    // =========================================================

    private BookInfoDTO buildValidBookDTO() {
        BookInfoDTO dto = new BookInfoDTO();

        // Common required fields
        dto.setProductType("BOOK");
        dto.setTitle("Clean Code");
        dto.setCategory("Computer Science");
        dto.setBarcode("89352346");
        dto.setImage("clean_code.png");
        dto.setOriginalValue(100000L);
        dto.setSellingPrice(120000L);
        dto.setWeight(0.5);
        dto.setDescription("Programming book");
        dto.setDimensions("20x15x3");

        // Book-specific required fields
        dto.setAuthor("Robert C. Martin");
        dto.setCoverType("Paperback");
        dto.setPublisher("Prentice Hall");
        dto.setPublicationDate(LocalDate.of(2008, 8, 1));

        // Optional fields — null by default
        dto.setPages(null);
        dto.setLanguage(null);
        dto.setGenre(null);

        return dto;
    }

    // =========================================================
    // BOOK VALIDATION TESTS
    // =========================================================

    @Nested
    class BookValidationTests {

        // -----------------------------------------------------
        // HAPPY PATH
        // -----------------------------------------------------

        // covers: all required fields valid, optional fields null
        // coverType = Paperback
        @Test
        void validBook_Paperback_shouldNotThrow() {
            BookInfoDTO dto = buildValidBookDTO();
            assertDoesNotThrow(() -> bookValidator.validate(dto));
        }

        // covers: coverType = Hardcover (second valid value)
        @Test
        void validBook_Hardcover_shouldNotThrow() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setCoverType("Hardcover");
            assertDoesNotThrow(() -> bookValidator.validate(dto));
        }

        // -----------------------------------------------------
        // AUTHOR VALIDATION
        // -----------------------------------------------------

        // covers: author = null
        @Test
        void nullAuthor_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setAuthor(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Author is required for Book", ex.getMessage());
        }

        // covers: author = empty string
        @Test
        void emptyAuthor_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setAuthor("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Author is required for Book", ex.getMessage());
        }

        // -----------------------------------------------------
        // COVER TYPE VALIDATION
        // -----------------------------------------------------

        // covers: coverType = null
        @Test
        void nullCoverType_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setCoverType(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Cover type is required for Book", ex.getMessage());
        }

        // covers: coverType = empty string
        @Test
        void emptyCoverType_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setCoverType("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Cover type is required for Book", ex.getMessage());
        }

        // covers: coverType = invalid value (not Paperback or Hardcover)
        @Test
        void invalidCoverType_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setCoverType("Spiral");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Cover type must be Paperback or Hardcover", ex.getMessage());
        }

        // -----------------------------------------------------
        // PUBLISHER VALIDATION
        // -----------------------------------------------------

        // covers: publisher = null
        @Test
        void nullPublisher_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setPublisher(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Publisher is required for Book", ex.getMessage());
        }

        // covers: publisher = empty string
        @Test
        void emptyPublisher_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setPublisher("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Publisher is required for Book", ex.getMessage());
        }

        // -----------------------------------------------------
        // PUBLICATION DATE VALIDATION
        // -----------------------------------------------------

        // covers: publicationDate = null
        @Test
        void nullPublicationDate_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setPublicationDate(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Publication date is required for Book", ex.getMessage());
        }

        // -----------------------------------------------------
        // PAGES VALIDATION — optional, but if provided must be > 0
        // -----------------------------------------------------

        // covers: pages = negative (invalid)
        @Test
        void negativePages_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setPages(-234);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Pages must be positive", ex.getMessage());
        }

        // covers: pages = 0 (lower boundary — invalid)
        @Test
        void zeroPages_shouldThrowException() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setPages(0);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Pages must be positive", ex.getMessage());
        }

        // covers: pages = 1 (lower boundary + 1 — valid)
        @Test
        void onePages_shouldNotThrow() {
            BookInfoDTO dto = buildValidBookDTO();
            dto.setPages(1);
            assertDoesNotThrow(() -> bookValidator.validate(dto));
        }
    }
}
package com.aims.service.product.validation;

import com.aims.dto.product.BookInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import com.aims.service.product.validator.BookValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CommonValidationTest {

    private BookValidator bookValidator;

    @BeforeEach
    void setUp() {
        bookValidator = new BookValidator();
    }

    // =========================================================
    // FACTORY METHOD
    // =========================================================

    private BookInfoDTO buildValidCommonDTO() {

        BookInfoDTO dto = new BookInfoDTO();

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

        // required Book fields
        dto.setAuthor("Robert C. Martin");
        dto.setCoverType("Paperback");
        dto.setPublisher("Prentice Hall");
        dto.setPublicationDate(LocalDate.of(2008, 8, 1));

        return dto;
    }

    @Nested
    class CommonValidationTests {

        // -----------------------------------------------------
        // VALID INPUT
        // -----------------------------------------------------

        @Test
        void validProductInfo_shouldNotThrow() {

            BookInfoDTO dto = buildValidCommonDTO();

            assertDoesNotThrow(() -> bookValidator.validate(dto));
        }

        // -----------------------------------------------------
        // TITLE VALIDATION
        // -----------------------------------------------------

        @Test
        void nullTitle_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setTitle(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Title must not be empty", ex.getMessage());
        }

        @Test
        void emptyTitle_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setTitle("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Title must not be empty", ex.getMessage());
        }

        // -----------------------------------------------------
        // CATEGORY VALIDATION
        // -----------------------------------------------------

        @Test
        void nullCategory_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setCategory(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Category must not be empty", ex.getMessage());
        }

        @Test
        void emptyCategory_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setCategory("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Category must not be empty", ex.getMessage());
        }

        // -----------------------------------------------------
        // BARCODE VALIDATION
        // -----------------------------------------------------

        @Test
        void nullBarcode_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setBarcode(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Barcode must not be empty", ex.getMessage());
        }

        @Test
        void emptyBarcode_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setBarcode("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Barcode must not be empty", ex.getMessage());
        }

        // -----------------------------------------------------
        // IMAGE VALIDATION
        // -----------------------------------------------------

        @Test
        void nullImage_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setImage(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Image must not be empty", ex.getMessage());
        }

        @Test
        void emptyImage_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setImage("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Image must not be empty", ex.getMessage());
        }

        // -----------------------------------------------------
        // ORIGINAL VALUE VALIDATION
        // -----------------------------------------------------

        @Test
        void negativeOriginalValue_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setOriginalValue(-1L);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Original value must be positive", ex.getMessage());
        }

        @Test
        void zeroOriginalValue_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setOriginalValue(0L);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Original value must be positive", ex.getMessage());
        }

        // -----------------------------------------------------
        // SELLING PRICE VALIDATION
        // -----------------------------------------------------

        @Test
        void sellingPriceBelow30Percent_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setOriginalValue(100000L);
            dto.setSellingPrice(29999L);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals(
                    "Selling price must not smaller than 30% of original value",
                    ex.getMessage());
        }

        @Test
        void sellingPriceExactly30Percent_shouldNotThrow() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setOriginalValue(100000L);
            dto.setSellingPrice(30000L);

            assertDoesNotThrow(() -> bookValidator.validate(dto));
        }

        @Test
        void sellingPriceAbove150Percent_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setOriginalValue(100000L);
            dto.setSellingPrice(150001L);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals(
                    "Selling price must not exceed 150% of original value",
                    ex.getMessage());
        }

        @Test
        void sellingPriceExactly150Percent_shouldNotThrow() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setOriginalValue(100000L);
            dto.setSellingPrice(150000L);

            assertDoesNotThrow(() -> bookValidator.validate(dto));
        }

        // -----------------------------------------------------
        // WEIGHT VALIDATION
        // -----------------------------------------------------

        @Test
        void negativeWeight_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setWeight(-1.0);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Weight must be positive", ex.getMessage());
        }

        @Test
        void zeroWeight_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setWeight(0.0);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Weight must be positive", ex.getMessage());
        }

        // -----------------------------------------------------
        // DESCRIPTION VALIDATION
        // -----------------------------------------------------

        @Test
        void nullDescription_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setDescription(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Description must not be empty", ex.getMessage());
        }

        @Test
        void emptyDescription_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setDescription("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Description must not be empty", ex.getMessage());
        }

        // -----------------------------------------------------
        // DIMENSIONS VALIDATION
        // -----------------------------------------------------

        @Test
        void nullDimensions_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setDimensions(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Dimensions must not be empty", ex.getMessage());
        }

        @Test
        void emptyDimensions_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setDimensions("");

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Dimensions must not be empty", ex.getMessage());
        }

        // -----------------------------------------------------
        // PRODUCT TYPE VALIDATION
        // -----------------------------------------------------

        @Test
        void nullProductType_shouldThrowException() {

            BookInfoDTO dto = buildValidCommonDTO();

            dto.setProductType(null);

            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> bookValidator.validate(dto));

            assertEquals("Product type is required", ex.getMessage());
        }
    }
}
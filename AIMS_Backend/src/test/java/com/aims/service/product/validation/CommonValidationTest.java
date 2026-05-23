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

class CommonValidationTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository);
    }

    private ProductInfoDTO buildValidCommonDTO() {
        ProductInfoDTO dto = new ProductInfoDTO();
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
        dto.setAuthor("Robert C. Martin");
        dto.setCoverType("Paperback");
        dto.setPublisher("Prentice Hall");
        dto.setPublicationDate(LocalDate.of(2008, 8, 1));
        dto.setPages(null);
        dto.setLanguage(null);
        dto.setGenre(null);
        return dto;
    }

    @Nested
    class CommonValidationTests {

        // -----------------------------------------------------
        // VALID INPUT
        // -----------------------------------------------------

        @Test
        void validProductInfo_shouldReturnTrue() {
            ProductInfoDTO dto = buildValidCommonDTO();
            boolean result = productService.validateProductInfo(dto);
            assertTrue(result);
        }

        // -----------------------------------------------------
        // TITLE VALIDATION
        // -----------------------------------------------------

        @Test
        void nullTitle_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setTitle(null);
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals("Title must not be empty", ex.getMessage());
        }

        @Test
        void emptyTitle_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setTitle("");
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals("Title must not be empty", ex.getMessage());
        }

        // -----------------------------------------------------
        // CATEGORY VALIDATION
        // -----------------------------------------------------

        @Test
        void nullCategory_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setCategory(null);
            assertThrows(InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
        }

        @Test
        void emptyCategory_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setCategory("");
            assertThrows(InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
        }

        // -----------------------------------------------------
        // BARCODE VALIDATION
        // -----------------------------------------------------

        @Test
        void nullBarcode_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setBarcode(null);
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals("Barcode must not be empty", ex.getMessage());
        }

        @Test
        void emptyBarcode_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setBarcode("");
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals("Barcode must not be empty", ex.getMessage());
        }

        // -----------------------------------------------------
        // IMAGE VALIDATION
        // -----------------------------------------------------

        @Test
        void nullImage_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setImage(null);
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals("Image must not be empty", ex.getMessage());
        }

        @Test
        void emptyImage_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setImage("");
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals("Image must not be empty", ex.getMessage());
        }

        // -----------------------------------------------------
        // ORIGINAL VALUE VALIDATION
        // -----------------------------------------------------

        @Test
        void negativeOriginalValue_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setOriginalValue(-1L);
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals("Original value must be positive", ex.getMessage());
        }

        @Test
        void zeroOriginalValue_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setOriginalValue(0L);
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals("Original value must be positive", ex.getMessage());
        }

        // -----------------------------------------------------
        // SELLING PRICE VALIDATION
        // -----------------------------------------------------

        @Test
        void sellingPriceBelow30Percent_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setOriginalValue(100000L);
            dto.setSellingPrice(29999L);
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals(
                    "Selling price must not smaller than 30% of original value",
                    ex.getMessage());
        }

        @Test
        void sellingPriceExactly30Percent_shouldPass() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setOriginalValue(100000L);
            dto.setSellingPrice(30000L);
            assertTrue(productService.validateProductInfo(dto));
        }

        @Test
        void sellingPriceAbove150Percent_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setOriginalValue(100000L);
            dto.setSellingPrice(150001L);
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals(
                    "Selling price must not exceed 150% of original value",
                    ex.getMessage());
        }

        @Test
        void sellingPriceExactly150Percent_shouldPass() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setOriginalValue(100000L);
            dto.setSellingPrice(150000L);
            assertTrue(productService.validateProductInfo(dto));
        }

        // -----------------------------------------------------
        // WEIGHT VALIDATION
        // -----------------------------------------------------

        @Test
        void negativeWeight_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setWeight(-1.0);
            assertThrows(InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
        }

        @Test
        void zeroWeight_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setWeight(0.0);
            assertThrows(InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
        }

        // -----------------------------------------------------
        // DESCRIPTION VALIDATION
        // -----------------------------------------------------

        @Test
        void nullDescription_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setDescription(null);
            assertThrows(InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
        }

        @Test
        void emptyDescription_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setDescription("");
            assertThrows(InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
        }

        // -----------------------------------------------------
        // DIMENSIONS VALIDATION
        // -----------------------------------------------------

        @Test
        void nullDimensions_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setDimensions(null);
            assertThrows(InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
        }

        @Test
        void emptyDimensions_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setDimensions("");
            assertThrows(InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
        }

        // -----------------------------------------------------
        // PRODUCT TYPE VALIDATION
        // -----------------------------------------------------

        @Test
        void nullProductType_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setProductType(null);
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals("Product type is required", ex.getMessage());
        }

        @Test
        void invalidProductType_shouldThrowException() {
            ProductInfoDTO dto = buildValidCommonDTO();
            dto.setProductType("MAGAZINE"); // ❌ không nằm trong 4 loại
            InvalidProductInfoException ex = assertThrows(
                    InvalidProductInfoException.class,
                    () -> productService.validateProductInfo(dto));
            assertEquals(
                    "Product type must be one of: BOOK, CD, NEWSPAPER, DVD",
                    ex.getMessage());
        }
    }
}
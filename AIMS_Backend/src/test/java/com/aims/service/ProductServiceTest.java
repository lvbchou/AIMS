package com.aims.service;

import com.aims.dto.ProductInfoDTO;
import com.aims.exception.InvalidProductInfoException;
import com.aims.exception.ProductAlreadyExistsException;
import com.aims.exception.ProductNotFoundException;
import com.aims.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductInfoDTO validBookDTO;

    @BeforeEach
    void setUp() {
        validBookDTO = ProductInfoDTO.builder()
                .productType("BOOK")
                .title("Clean Code")
                .category("Programming")
                .barcode("978-0132350884")
                .image("clean-code.jpg")
                .originalValue(300_000L)
                .sellingPrice(350_000L)
                .weight(0.5)
                .description("A handbook of agile software craftsmanship")
                .dimensions("24x16x3 cm")
                .quantityInStock(50)
                .publisher("Prentice Hall")
                .publicationDate(LocalDate.of(2008, 8, 1))
                .language("English")
                .author("Robert C. Martin")
                .coverType("Paperback")
                .pages(431)
                .genre("Technology")
                .build();
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    @Nested
    @DisplayName("saveProduct()")
    class SaveProduct {

        @Test
        @DisplayName("Should save a valid Book product successfully")
        void shouldSaveValidBook() {
            when(productRepository.existsByBarcode(validBookDTO.getBarcode())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            assertThatNoException().isThrownBy(() -> productService.saveProduct(validBookDTO));
            verify(productRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw ProductAlreadyExistsException when barcode is duplicate")
        void shouldThrowWhenBarcodeExists() {
            when(productRepository.existsByBarcode(validBookDTO.getBarcode())).thenReturn(true);

            assertThatThrownBy(() -> productService.saveProduct(validBookDTO))
                    .isInstanceOf(ProductAlreadyExistsException.class)
                    .hasMessageContaining(validBookDTO.getBarcode());
        }

        @Test
        @DisplayName("Should throw InvalidProductInfoException when title is blank")
        void shouldThrowWhenTitleBlank() {
            validBookDTO.setTitle("");

            assertThatThrownBy(() -> productService.saveProduct(validBookDTO))
                    .isInstanceOf(InvalidProductInfoException.class)
                    .hasMessageContaining("Title");
        }

        @Test
        @DisplayName("Should throw InvalidProductInfoException when selling price > 150% original value")
        void shouldThrowWhenPriceExceedsLimit() {
            validBookDTO.setSellingPrice(600_000L); // 200% of 300_000

            assertThatThrownBy(() -> productService.saveProduct(validBookDTO))
                    .isInstanceOf(InvalidProductInfoException.class)
                    .hasMessageContaining("150%");
        }

        @Test
        @DisplayName("Should throw InvalidProductInfoException when productType is null")
        void shouldThrowWhenProductTypeNull() {
            validBookDTO.setProductType(null);

            assertThatThrownBy(() -> productService.saveProduct(validBookDTO))
                    .isInstanceOf(InvalidProductInfoException.class)
                    .hasMessageContaining("Product type");
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("Should delete product when it exists")
        void shouldDeleteExistingProduct() {
            Book book = new Book();
            book.setProductId(1);
            when(productRepository.findById(1)).thenReturn(Optional.of(book));
            doNothing().when(productRepository).delete(book);

            assertThatNoException().isThrownBy(() -> productService.deleteProduct(book));
            verify(productRepository).delete(book);
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when product does not exist")
        void shouldThrowWhenProductNotFound() {
            Book book = new Book();
            book.setProductId(1);
            when(productRepository.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(book))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

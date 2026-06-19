package com.aims.service.product;

import com.aims.dto.product.BookInfoDTO;
import com.aims.dto.product.CDInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;
import com.aims.exception.ProductAlreadyExistsException;
import com.aims.repository.ProductRepository;
import com.aims.service.ProductService;
import com.aims.service.product.creator.*;
import com.aims.service.product.validator.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * Testing strategy for SaveProduct
 *
 * Unit under test: ProductService.saveProduct()
 *
 * Note: validation logic is tested separately in CommonValidationTest,
 * BookValidationTest, etc. This class only tests saveProduct() behavior
 * after validation passes.
 *
 * Partitions:
 *   barcode : already exists in DB | not exists in DB
 */
class SaveProductTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        ProductValidatorRegistry validatorRegistry =
                new ProductValidatorRegistry(
                        new BookValidator(),
                        new CDValidator(),
                        new DVDValidator(),
                        new NewspaperValidator()
                );

        ProductCreatorRegistry creatorRegistry =
                new ProductCreatorRegistry(
                        new BookCreator(),
                        new CDCreator(),
                        new DVDCreator(),
                        new NewspaperCreator()
                );

        productService = new ProductService(
                productRepository,
                validatorRegistry,
                creatorRegistry
        );
    }

    // =========================================================
    // FACTORY METHODS
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

    private CDInfoDTO buildValidCDDTO() {
        CDInfoDTO dto = new CDInfoDTO();
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
        dto.setArtists(new ArrayList<>(List.of("The Beatles")));
        dto.setRecordLabel("Apple Records");
        dto.setGenre("Rock");
        dto.setTracks(new ArrayList<>(List.of(
                new CDInfoDTO.TrackDTO("Come Together", "4:19")
        )));
        dto.setReleaseDate(null);
        return dto;
    }

    // =========================================================
    // SAVE PRODUCT TESTS
    // =========================================================

    @Nested
    class SaveProductTests {

        // covers: valid Book DTO, barcode not exists → save successfully
        @Test
        void saveValidBook_barcodeNotExists_shouldReturnProduct() {
            ProductInfoDTO dto = buildValidBookDTO();

            when(productRepository.existsByBarcode(dto.getBarcode()))
                    .thenReturn(false);
            when(productRepository.save(any(Product.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Product result = productService.saveProduct(dto);

            assertNotNull(result);
            assertEquals(dto.getTitle(), result.getTitle());
            assertEquals(dto.getBarcode(), result.getBarcode());
            assertEquals("active", result.getStatus());
            verify(productRepository, times(1)).save(any(Product.class));
        }

        // covers: valid CD DTO, barcode not exists → save successfully
        @Test
        void saveValidCD_barcodeNotExists_shouldReturnProduct() {
            ProductInfoDTO dto = buildValidCDDTO();

            when(productRepository.existsByBarcode(dto.getBarcode()))
                    .thenReturn(false);
            when(productRepository.save(any(Product.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Product result = productService.saveProduct(dto);

            assertNotNull(result);
            assertEquals(dto.getTitle(), result.getTitle());
            verify(productRepository, times(1)).save(any(Product.class));
        }

        // covers: barcode already exists in DB → throw exception
        @Test
        void saveProduct_duplicateBarcode_shouldThrowException() {
            ProductInfoDTO dto = buildValidBookDTO();

            when(productRepository.existsByBarcode(dto.getBarcode()))
                    .thenReturn(true);

            ProductAlreadyExistsException ex = assertThrows(
                    ProductAlreadyExistsException.class,
                    () -> productService.saveProduct(dto));

            assertEquals(
                    "Product with barcode already exists: " + dto.getBarcode(),
                    ex.getMessage());
            verify(productRepository, never()).save(any());
        }
    }
}
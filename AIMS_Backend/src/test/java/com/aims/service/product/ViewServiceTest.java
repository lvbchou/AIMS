package com.aims.service.product;

import com.aims.dto.product.BookInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Book;
import com.aims.exception.ProductNotFoundException;
import com.aims.mapper.product.ProductMapper;
import com.aims.mapper.product.ProductMapperRegistry;
import com.aims.mapper.product.ProductSummaryMapper;
import com.aims.repository.product.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests — UC: View Product Details.
 * Unit under test: ProductQueryService.viewProduct(Integer productId) ->
 * ProductInfoDTO
 * 
 * - Repository.findActiveById(id) da loc status='active' NGAY trong cau query.
 * Vi vay "khong ton tai", "id <= 0", va "san pham inactive" deu tra ve
 * Optional.empty() -> service nem DUY NHAT ProductNotFoundException.
 * - Code KHONG validate "id phai la so nguyen duong" o tang service.
 *
 * Strategy: Black-box - Equivalence Partitioning (EP) + Boundary Value Analysis
 * (BVA).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC ViewProductDetails - ProductQueryService.viewProduct (UT001-UT005)")
class ViewServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapperRegistry mapperRegistry;
    @Mock
    private ProductSummaryMapper productSummaryMapper;
    @Mock
    private ProductMapper<ProductInfoDTO, Book> mapper;

    private ProductQueryService queryService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        queryService = new ProductQueryService(productRepository, mapperRegistry, productSummaryMapper);

        // "id == 42, active product" theo tai lieu
        sampleBook = new Book(
                "Clean Code", "Programming", "978-0132350884", "img.jpg",
                300_000L, 350_000L, 0.5, "Agile handbook", "24x16 cm", 50,
                "Prentice Hall", LocalDate.of(2008, 8, 1), "English",
                "Robert C. Martin", "Paperback", 431, "Technology");
        sampleBook.setProductId(42);
        sampleBook.setStatus("active");
    }

    /** DTO active day du field bat buoc, tuong ung sampleBook (id=42). */
    private BookInfoDTO activeBookDto() {
        BookInfoDTO dto = new BookInfoDTO();
        dto.setProductId(42);
        dto.setProductType("BOOK");
        dto.setStatus("active");
        dto.setTitle("Clean Code");
        dto.setSellingPrice(350_000L);
        dto.setDescription("Agile handbook");
        dto.setImage("img.jpg");
        dto.setAuthor("Robert C. Martin");
        return dto;
    }

    // ================================================================
    // UT001 - Valid product (Happy path)
    // ================================================================
    @Test
    @DisplayName("UT001 Valid product - returns DTO of existing active product")
    void UT001_validProduct() {
        when(productRepository.findActiveById(42)).thenReturn(Optional.of(sampleBook));
        when(mapperRegistry.getMapper(sampleBook)).thenReturn(mapper);
        when(mapper.toDTO(sampleBook)).thenReturn(activeBookDto());

        ProductInfoDTO result = queryService.viewProduct(42);

        // A ProductInfoDTO instance with: id == 42, status active, mandatory fields not
        // null.
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(42);
        assertThat(result.getStatus()).isEqualTo("active");
        assertThat(result.getTitle()).isNotNull();
        assertThat(result.getSellingPrice()).isNotNull();
        assertThat(result.getDescription()).isNotNull();
        assertThat(result.getImage()).isNotNull();

        verify(productRepository, times(1)).findActiveById(42);
    }

    // ================================================================
    // UT002 - Product not found (ID absent)
    // ================================================================
    @Test
    @DisplayName("UT002 Product not found - raises ProductNotFoundException when ID is absent")
    void UT002_productNotFound() {
        when(productRepository.findActiveById(999_999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.viewProduct(999_999))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999999"); // "Product not found with id: 999999"

        verify(mapperRegistry, never()).getMapper(any());
        verify(mapper, never()).toDTO(any());
    }

    // ================================================================
    // UT003 - Boundary ID zero
    // ================================================================
    @Test
    @DisplayName("UT003 Boundary ID zero - id=0 not found -> ProductNotFoundException")
    void UT003_boundaryIdZero() {
        // Code KHONG nem ValueError cho id<=0; id=0 don gian la khong co ban ghi
        // active -> ProductNotFoundException (can duoi bien).
        when(productRepository.findActiveById(0)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.viewProduct(0))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("0");

        verify(mapperRegistry, never()).getMapper(any());
    }

    // ================================================================
    // UT004 - Inactive product
    // ================================================================
    @Test
    @DisplayName("UT004 Inactive product - inactive record is not active -> ProductNotFoundException")
    void UT004_inactiveProduct() {
        // findActiveById loc status='active' TRONG query, nen mot san pham inactive
        // (id=87) khong bao gio duoc tra ve -> Optional.empty().
        // He qua: service nem ProductNotFoundException (khong co InactiveProductError
        // rieng).
        when(productRepository.findActiveById(87)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.viewProduct(87))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("87");

        verify(mapper, never()).toDTO(any());
    }
}
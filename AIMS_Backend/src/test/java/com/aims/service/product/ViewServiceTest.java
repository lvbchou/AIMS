package com.aims.service.product;

import com.aims.dto.product.BookInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.*;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests — UC: View Product Details.
 * Unit under test: ProductQueryService.viewProduct(Integer productId) -> ProductInfoDTO
 *
 * Strategy: Black-box – Equivalence Partitioning (EP) + Boundary Value Analysis (BVA).
 *   UT001–UT008
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC ViewProductDetails – ProductQueryService.viewProduct")
class ViewServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapperRegistry mapperRegistry;
    @Mock private ProductSummaryMapper productSummaryMapper;

    private ProductQueryService queryService;

    private Book      sampleBook;
    private DVD       sampleDvd;
    private CD        sampleCd;
    private Newspaper sampleNewspaper;

    @BeforeEach
    void setUp() {
        queryService = new ProductQueryService(productRepository, mapperRegistry, productSummaryMapper);

        sampleBook = new Book(
                "Clean Code", "Programming", "978-0132350884", "img.jpg",
                300_000L, 350_000L, 0.5, "Agile handbook", "24x16 cm", 50,
                "Prentice Hall", LocalDate.of(2008, 8, 1), "English",
                "Robert C. Martin", "Paperback", 431, "Technology");
        sampleBook.setProductId(1);

        sampleDvd = new DVD(
                "Inception", "Film", "DVD-001", "inception.jpg",
                200_000L, 250_000L, 0.1, "Sci-fi thriller", "19x13 cm", 20,
                "Sci-Fi", LocalDate.of(2010, 7, 16),
                "Blu-ray", "Christopher Nolan", 148, "Warner Bros", "English", "Vietnamese");
        sampleDvd.setProductId(2);

        sampleCd = new CD(
                "Abbey Road", "Music", "CD-001", "abbey.jpg",
                150_000L, 180_000L, 0.08, "Classic album", "12x12 cm", 30,
                "Rock", LocalDate.of(1969, 9, 26),
                List.of("The Beatles"), "Apple Records");
        sampleCd.setProductId(3);

        sampleNewspaper = new Newspaper(
                "Tuoi Tre Daily", "News", "NEWS-001", "tuoitre.jpg",
                10_000L, 12_000L, 0.2, "Daily newspaper", "30x42 cm", 100,
                "Tuoi Tre", LocalDate.of(2024, 1, 1), "Vietnamese",
                "Nguyen Van A", "1234", "Daily", "ISSN-001",
                List.of("Politics", "Sports", "Economy"));
        sampleNewspaper.setProductId(4);
    }

    // Stub mapperRegistry trả 1 ProductInfoDTO mang field chung.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubViewMapper(Product product) {
        ProductInfoDTO dto = new BookInfoDTO(); // concrete bất kỳ; chỉ cần field chung
        dto.setProductId(product.getProductId());
        dto.setTitle(product.getTitle());
        dto.setBarcode(product.getBarcode());
        dto.setSellingPrice(product.getSellingPrice());

        ProductMapper mapper = mock(ProductMapper.class);
        when(mapper.toDTO(product)).thenReturn(dto);
        when(mapperRegistry.getMapper(product)).thenReturn(mapper);
    }

    @Test
    @DisplayName("UT001 View Valid Book Product")
    void UT001_viewValidBookProduct() {
        when(productRepository.findActiveById(1)).thenReturn(Optional.of(sampleBook));
        stubViewMapper(sampleBook);

        ProductInfoDTO result = queryService.viewProduct(1);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1);
        assertThat(result.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("UT002 View Valid DVD Product")
    void UT002_viewValidDvdProduct() {
        when(productRepository.findActiveById(2)).thenReturn(Optional.of(sampleDvd));
        stubViewMapper(sampleDvd);

        ProductInfoDTO result = queryService.viewProduct(2);

        assertThat(result.getTitle()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("UT003 View Valid CD Product")
    void UT003_viewValidCdProduct() {
        when(productRepository.findActiveById(3)).thenReturn(Optional.of(sampleCd));
        stubViewMapper(sampleCd);

        ProductInfoDTO result = queryService.viewProduct(3);

        assertThat(result.getTitle()).isEqualTo("Abbey Road");
    }

    @Test
    @DisplayName("UT004 View Valid Newspaper Product")
    void UT004_viewValidNewspaperProduct() {
        when(productRepository.findActiveById(4)).thenReturn(Optional.of(sampleNewspaper));
        stubViewMapper(sampleNewspaper);

        ProductInfoDTO result = queryService.viewProduct(4);

        assertThat(result.getTitle()).isEqualTo("Tuoi Tre Daily");
    }

    @Test
    @DisplayName("UT005 View Product With Nonexistent Id")
    void UT005_viewProductWithNonexistentId() {
        when(productRepository.findActiveById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.viewProduct(999))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("UT006 View Product With Null Id")
    void UT006_viewProductWithNullId() {
        // Dùng matcher để stub khớp đối số null (literal null không khớp ổn định trong Mockito).
        when(productRepository.findActiveById(nullable(Integer.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.viewProduct(null))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("UT007 View Product With Negative Id")
    void UT007_viewProductWithNegativeId() {
        when(productRepository.findActiveById(-1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.viewProduct(-1))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("UT008 View Product Calls Repository Once")
    void UT008_viewProductCallsRepositoryOnce() {
        when(productRepository.findActiveById(1)).thenReturn(Optional.of(sampleBook));
        stubViewMapper(sampleBook);

        queryService.viewProduct(1);

        verify(productRepository, times(1)).findActiveById(1);
    }
}
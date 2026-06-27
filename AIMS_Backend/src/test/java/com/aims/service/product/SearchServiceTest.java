package com.aims.service.product;

import com.aims.dto.product.ProductSummaryDTO;
import com.aims.entity.product.*;
import com.aims.exception.EmptySearchInputException;
import com.aims.exception.InvalidProductInfoException;
import com.aims.mapper.product.ProductSummaryMapper;
import com.aims.repository.product.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests — UC: Search Product (bao gồm Filter Product).
 * Unit under test: ProductSearchService
 *   - searchProduct(keyword, category, pageable)                    -> UT009–UT017
 *   - filterProductsByPriceRange(keyword, category, range, pageable)-> UT018–UT026
 *
 * Strategy: Black-box – Equivalence Partitioning (EP) + Boundary Value Analysis (BVA).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC SearchProduct (+ Filter) – ProductSearchService")
class SearchServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductSummaryMapper productSummaryMapper;

    // PriceRangeParser là component thuần (không phụ thuộc) -> dùng instance thật.
    private final PriceRangeParser priceRangeParser = new PriceRangeParser();

    private ProductSearchService searchService;

    private Book      sampleBook;
    private DVD       sampleDvd;
    private CD        sampleCd;
    private Newspaper sampleNewspaper;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        searchService = new ProductSearchService(productRepository, productSummaryMapper, priceRangeParser);

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

    // Helper: ProductSummaryDTO (record) ứng với 1 product.
    private ProductSummaryDTO summaryOf(Product p) {
        return new ProductSummaryDTO(
                p.getProductId(), p.getTitle(),
                p.getClass().getSimpleName().toUpperCase(),
                p.getSellingPrice(), p.getImage(), p.getQuantityInStock());
    }

    // ================================================================
    // SEARCH PRODUCT
    // ================================================================
    @Nested
    @DisplayName("searchProduct")
    class SearchProduct {

        @Test
        @DisplayName("UT009 Search Keyword Matches Title")
        void UT009_searchKeywordMatchesTitle() {
            when(productRepository.searchByKeywordAndCategory("Clean", null, pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleBook)));
            when(productSummaryMapper.toDTO(sampleBook)).thenReturn(summaryOf(sampleBook));

            Page<ProductSummaryDTO> results = searchService.searchProduct("Clean", null, pageable);

            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).title()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("UT010 Search Keyword Matches Multiple Titles")
        void UT010_searchKeywordMatchesMultipleTitles() {
            when(productRepository.searchByKeywordAndCategory("a", null, pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleBook, sampleCd, sampleNewspaper)));
            when(productSummaryMapper.toDTO(any(Product.class)))
                    .thenAnswer(inv -> summaryOf(inv.getArgument(0)));

            Page<ProductSummaryDTO> results = searchService.searchProduct("a", null, pageable);

            assertThat(results.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("UT011 Search Keyword Matches Category DVD")
        void UT011_searchKeywordMatchesCategoryDvd() {
            when(productRepository.searchByKeywordAndCategory(null, "Film", pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleDvd)));
            when(productSummaryMapper.toDTO(sampleDvd)).thenReturn(summaryOf(sampleDvd));

            Page<ProductSummaryDTO> results = searchService.searchProduct(null, "Film", pageable);

            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).title()).isEqualTo("Inception");
        }

        @Test
        @DisplayName("UT012 Search Keyword Matches Category Returns Multiple")
        void UT012_searchKeywordMatchesCategoryReturnsMultiple() {
            when(productRepository.searchByKeywordAndCategory(null, "Music", pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleCd, sampleCd)));
            when(productSummaryMapper.toDTO(sampleCd)).thenReturn(summaryOf(sampleCd));

            Page<ProductSummaryDTO> results = searchService.searchProduct(null, "Music", pageable);

            assertThat(results.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("UT013 Search Matches Both Title And Category")
        void UT013_searchMatchesBothTitleAndCategory() {
            when(productRepository.searchByKeywordAndCategory("Abbey", "Music", pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleCd)));
            when(productSummaryMapper.toDTO(sampleCd)).thenReturn(summaryOf(sampleCd));

            Page<ProductSummaryDTO> results = searchService.searchProduct("Abbey", "Music", pageable);

            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).title()).isEqualTo("Abbey Road");
        }

        @Test
        @DisplayName("UT014 Search Keyword No Match Returns Empty List")
        void UT014_searchKeywordNoMatchReturnsEmptyList() {
            when(productRepository.searchByKeywordAndCategory("xyz123", null, pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<ProductSummaryDTO> results = searchService.searchProduct("xyz123", null, pageable);

            assertThat(results.getContent()).isEmpty();
        }

        @Test
        @DisplayName("UT015 Search With Category Only (Empty Keyword) Returns Results")
        void UT015_searchWithCategoryOnlyReturnsResults() {
            // keyword rỗng nhưng category có giá trị -> hợp lệ, KHÔNG ném exception.
            // (Khác ảnh gốc: code chỉ chặn khi CẢ keyword VÀ category cùng rỗng.)
            when(productRepository.searchByKeywordAndCategory("", "News", pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleNewspaper)));
            when(productSummaryMapper.toDTO(sampleNewspaper)).thenReturn(summaryOf(sampleNewspaper));

            Page<ProductSummaryDTO> results = searchService.searchProduct("", "News", pageable);

            assertThat(results.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("UT016 Search With Both Keyword And Category Empty Throws Exception")
        void UT016_searchWithBothEmptyThrowsException() {
            // Alternative Flow: cả keyword và category rỗng -> EmptySearchInputException
            assertThatThrownBy(() -> searchService.searchProduct("", "", pageable))
                    .isInstanceOf(EmptySearchInputException.class)
                    .hasMessageContaining("Please enter a product title or category");
        }

        @Test
        @DisplayName("UT017 Search With Both Keyword And Category Null Throws Exception")
        void UT017_searchWithBothNullThrowsException() {
            assertThatThrownBy(() -> searchService.searchProduct(null, null, pageable))
                    .isInstanceOf(EmptySearchInputException.class);

            verify(productRepository, never())
                    .searchByKeywordAndCategory(any(), any(), any());
        }
    }

    // ================================================================
    // FILTER PRODUCT (by price range)
    // ================================================================
    @Nested
    @DisplayName("filterProductsByPriceRange")
    class FilterProduct {

        @Test
        @DisplayName("UT018 Filter Returns Products In Range")
        void UT018_filterReturnsProductsInRange() {
            when(productRepository.searchByKeywordCategoryAndPriceRange(
                    null, "Programming", 100_000L, 500_000L, pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleBook)));
            when(productSummaryMapper.toDTO(sampleBook)).thenReturn(summaryOf(sampleBook));

            Page<ProductSummaryDTO> results = searchService.filterProductsByPriceRange(
                    null, "Programming", "100000-500000", pageable);

            assertThat(results.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("UT019 Filter Returns Book In Range")
        void UT019_filterReturnsBookInRange() {
            when(productRepository.searchByKeywordCategoryAndPriceRange(
                    "Clean", null, 300_000L, 400_000L, pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleBook)));
            when(productSummaryMapper.toDTO(sampleBook)).thenReturn(summaryOf(sampleBook));

            Page<ProductSummaryDTO> results = searchService.filterProductsByPriceRange(
                    "Clean", null, "300000-400000", pageable);

            assertThat(results.getContent().get(0).title()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("UT020 Filter Returns Empty When No Product In Range")
        void UT020_filterReturnsEmptyWhenNoProductInRange() {
            when(productRepository.searchByKeywordCategoryAndPriceRange(
                    null, "Music", 9_000_000L, 10_000_000L, pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<ProductSummaryDTO> results = searchService.filterProductsByPriceRange(
                    null, "Music", "9000000-10000000", pageable);

            assertThat(results.getContent()).isEmpty();
        }

        @Test
        @DisplayName("UT021 Filter With Min Equals Max Boundary")
        void UT021_filterWithMinEqualsMaxBoundary() {
            when(productRepository.searchByKeywordCategoryAndPriceRange(
                    null, "Film", 250_000L, 250_000L, pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleDvd)));
            when(productSummaryMapper.toDTO(sampleDvd)).thenReturn(summaryOf(sampleDvd));

            Page<ProductSummaryDTO> results = searchService.filterProductsByPriceRange(
                    null, "Film", "250000-250000", pageable);

            assertThat(results.getContent().get(0).sellingPrice()).isEqualTo(250_000L);
        }

        @Test
        @DisplayName("UT022 Filter With Zero Min Price Boundary")
        void UT022_filterWithZeroMinPriceBoundary() {
            when(productRepository.searchByKeywordCategoryAndPriceRange(
                    null, "Programming", 0L, 500_000L, pageable))
                    .thenReturn(new PageImpl<>(List.of(sampleBook)));
            when(productSummaryMapper.toDTO(sampleBook)).thenReturn(summaryOf(sampleBook));

            Page<ProductSummaryDTO> results = searchService.filterProductsByPriceRange(
                    null, "Programming", "0-500000", pageable);

            assertThat(results.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("UT023 Filter With Invalid Price Range Format Throws Exception")
        void UT023_filterWithInvalidPriceRangeFormat() {
            assertThatThrownBy(() -> searchService.filterProductsByPriceRange(
                    null, "Programming", "invalid", pageable))
                    .isInstanceOf(InvalidProductInfoException.class)
                    .hasMessageContaining("price range");
        }

        @Test
        @DisplayName("UT024 Filter With Non-Numeric Price Throws Exception")
        void UT024_filterWithNonNumericPrice() {
            assertThatThrownBy(() -> searchService.filterProductsByPriceRange(
                    null, "Programming", "abc-xyz", pageable))
                    .isInstanceOf(InvalidProductInfoException.class);
        }

        @Test
        @DisplayName("UT025 Filter With Max Less Than Min Throws Exception")
        void UT025_filterWithMaxLessThanMin() {
            assertThatThrownBy(() -> searchService.filterProductsByPriceRange(
                    null, "Programming", "500000-100000", pageable))
                    .isInstanceOf(InvalidProductInfoException.class);
        }

        @Test
        @DisplayName("UT026 Filter With Both Keyword And Category Empty Throws Exception")
        void UT026_filterWithBothEmptyThrowsException() {
            assertThatThrownBy(() -> searchService.filterProductsByPriceRange(
                    "", "", "100000-500000", pageable))
                    .isInstanceOf(EmptySearchInputException.class);
        }
    }
}
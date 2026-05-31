package com.aims.service;

import com.aims.entity.*;
import com.aims.exception.InvalidProductInfoException;
import com.aims.exception.ProductNotFoundException;
import com.aims.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UC: View Product Details, Search Product, Filter Product
 * Strategy: Black-box – Equivalence Partitioning (EP) + Boundary Value Analysis (BVA)
 *
 * UT001–UT008  viewProduct(barcode)
 * UT009–UT017  searchProduct(keyword, category)
 * UT018–UT026  filterProduct(products, priceRange)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService – View Product / Search Product / Filter Product")
class ProductServiceViewSearchFilterTest {

    @Mock  private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    private Book      sampleBook;
    private DVD       sampleDvd;
    private CD        sampleCd;
    private Newspaper sampleNewspaper;

    @BeforeEach
    void setUp() {
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

    // ================================================================
    // UC: VIEW PRODUCT DETAILS
    // Unit: ProductService.viewProduct(barcode)
    // ================================================================

    @Nested
    @DisplayName("viewProduct")
    class ViewProduct {

        @Test
        @DisplayName("UT001 View Valid Book Product")
        void UT001_viewValidBookProduct() {
            // barcode = '978-0132350884' (Book exists in repository)
            when(productRepository.findByBarcode("978-0132350884")).thenReturn(Optional.of(sampleBook));

            Product result = productService.viewProduct("978-0132350884");

            assertThat(result).isNotNull().isInstanceOf(Book.class);
            assertThat(result.getBarcode()).isEqualTo("978-0132350884");
            assertThat(result.getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("UT002 View Valid DVD Product")
        void UT002_viewValidDvdProduct() {
            // barcode = 'DVD-001' (DVD exists in repository)
            when(productRepository.findByBarcode("DVD-001")).thenReturn(Optional.of(sampleDvd));

            Product result = productService.viewProduct("DVD-001");

            assertThat(result).isInstanceOf(DVD.class);
            assertThat(result.getTitle()).isEqualTo("Inception");
        }

        @Test
        @DisplayName("UT003 View Valid CD Product")
        void UT003_viewValidCdProduct() {
            // barcode = 'CD-001' (CD exists in repository)
            when(productRepository.findByBarcode("CD-001")).thenReturn(Optional.of(sampleCd));

            Product result = productService.viewProduct("CD-001");

            assertThat(result).isInstanceOf(CD.class);
            assertThat(result.getTitle()).isEqualTo("Abbey Road");
        }

        @Test
        @DisplayName("UT004 View Valid Newspaper Product")
        void UT004_viewValidNewspaperProduct() {
            // barcode = 'NEWS-001' (Newspaper exists in repository)
            when(productRepository.findByBarcode("NEWS-001")).thenReturn(Optional.of(sampleNewspaper));

            Product result = productService.viewProduct("NEWS-001");

            assertThat(result).isInstanceOf(Newspaper.class);
            assertThat(result.getTitle()).isEqualTo("Tuoi Tre Daily");
        }

        @Test
        @DisplayName("UT005 View Product With Nonexistent Barcode")
        void UT005_viewProductWithNonexistentBarcode() {
            // barcode = 'UNKNOWN-999' (not in repository)
            when(productRepository.findByBarcode("UNKNOWN-999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.viewProduct("UNKNOWN-999"))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("UNKNOWN-999");
        }

        @Test
        @DisplayName("UT006 View Product With Null Barcode")
        void UT006_viewProductWithNullBarcode() {
            // barcode = null
            when(productRepository.findByBarcode(null)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.viewProduct(null))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("UT007 View Product With Empty Barcode")
        void UT007_viewProductWithEmptyBarcode() {
            // barcode = '' (empty string, length = 0)
            when(productRepository.findByBarcode("")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.viewProduct(""))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("UT008 View Product Calls Repository Once")
        void UT008_viewProductCallsRepositoryOnce() {
            // barcode = '978-0132350884'; verify delegation to repository
            when(productRepository.findByBarcode("978-0132350884")).thenReturn(Optional.of(sampleBook));

            productService.viewProduct("978-0132350884");

            verify(productRepository, times(1)).findByBarcode("978-0132350884");
        }
    }

    // ================================================================
    // UC: SEARCH PRODUCT
    // Unit: ProductService.searchProduct(keyword, category)
    // ================================================================

    @Nested
    @DisplayName("searchProduct")
    class SearchProduct {

        @Test
        @DisplayName("UT009 Search By Keyword And Category Programming")
        void UT009_searchByKeywordAndCategoryProgramming() {
            // keyword = 'Clean', category = 'Programming'
            when(productRepository.searchByKeywordAndCategory("Clean", "Programming"))
                    .thenReturn(List.of(sampleBook));

            List<Product> results = productService.searchProduct("Clean", "Programming");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("UT010 Search By Keyword And Category Film")
        void UT010_searchByKeywordAndCategoryFilm() {
            // keyword = 'Inception', category = 'Film'
            when(productRepository.searchByKeywordAndCategory("Inception", "Film"))
                    .thenReturn(List.of(sampleDvd));

            List<Product> results = productService.searchProduct("Inception", "Film");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Inception");
        }

        @Test
        @DisplayName("UT011 Search By Keyword And Category Music")
        void UT011_searchByKeywordAndCategoryMusic() {
            // keyword = 'Abbey', category = 'Music'
            when(productRepository.searchByKeywordAndCategory("Abbey", "Music"))
                    .thenReturn(List.of(sampleCd));

            List<Product> results = productService.searchProduct("Abbey", "Music");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Abbey Road");
        }

        @Test
        @DisplayName("UT012 Search By Keyword And Category News")
        void UT012_searchByKeywordAndCategoryNews() {
            // keyword = 'Tuoi Tre', category = 'News'
            when(productRepository.searchByKeywordAndCategory("Tuoi Tre", "News"))
                    .thenReturn(List.of(sampleNewspaper));

            List<Product> results = productService.searchProduct("Tuoi Tre", "News");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Tuoi Tre Daily");
        }

        @Test
        @DisplayName("UT013 Search With No Matching Results")
        void UT013_searchWithNoMatchingResults() {
            // keyword = 'xyz123', category = 'unknown'
            when(productRepository.searchByKeywordAndCategory("xyz123", "unknown"))
                    .thenReturn(Collections.emptyList());

            List<Product> results = productService.searchProduct("xyz123", "unknown");

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("UT014 Search With Both Params Null")
        void UT014_searchWithBothParamsNull() {
            // keyword = null, category = null → SQL IS NULL conditions, returns all products
            when(productRepository.searchByKeywordAndCategory(null, null))
                    .thenReturn(List.of(sampleBook, sampleDvd, sampleCd, sampleNewspaper));

            List<Product> results = productService.searchProduct(null, null);

            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("UT015 Search By Keyword Only")
        void UT015_searchByKeywordOnly() {
            // keyword = 'Code', category = null
            when(productRepository.searchByKeywordAndCategory("Code", null))
                    .thenReturn(List.of(sampleBook));

            List<Product> results = productService.searchProduct("Code", null);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).contains("Code");
        }

        @Test
        @DisplayName("UT016 Search By Category Only")
        void UT016_searchByCategoryOnly() {
            // keyword = null, category = 'Music'
            when(productRepository.searchByKeywordAndCategory(null, "Music"))
                    .thenReturn(List.of(sampleCd));

            List<Product> results = productService.searchProduct(null, "Music");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getCategory()).isEqualTo("Music");
        }

        @Test
        @DisplayName("UT017 Search Keyword Case Insensitive")
        void UT017_searchKeywordCaseInsensitive() {
            // keyword = 'CLEAN CODE' (uppercase); SQL LOWER() makes it case-insensitive
            when(productRepository.searchByKeywordAndCategory("CLEAN CODE", null))
                    .thenReturn(List.of(sampleBook));
            when(productRepository.searchByKeywordAndCategory("clean code", null))
                    .thenReturn(List.of(sampleBook));

            List<Product> upper = productService.searchProduct("CLEAN CODE", null);
            List<Product> lower = productService.searchProduct("clean code", null);

            assertThat(upper.get(0).getTitle()).isEqualTo(lower.get(0).getTitle());
        }
    }

    // ================================================================
    // UC: FILTER PRODUCT
    // Unit: ProductService.filterProduct(products, priceRange)
    // ================================================================

    @Nested
    @DisplayName("filterProduct")
    class FilterProduct {

        @Test
        @DisplayName("UT018 Filter Products In Valid Price Range")
        void UT018_filterProductsInValidPriceRange() {
            // priceRange = '100000-500000'
            when(productRepository.findByPriceRange(100_000L, 500_000L))
                    .thenReturn(List.of(sampleBook, sampleDvd, sampleCd));

            List<Product> results = productService.filterProduct(null, "100000-500000");

            assertThat(results).hasSize(3);
            assertThat(results).allMatch(p ->
                    p.getSellingPrice() >= 100_000L && p.getSellingPrice() <= 500_000L);
        }

        @Test
        @DisplayName("UT019 Filter With No Products In Range")
        void UT019_filterWithNoProductsInRange() {
            // priceRange = '900000-1000000' (no products in this range)
            when(productRepository.findByPriceRange(900_000L, 1_000_000L))
                    .thenReturn(Collections.emptyList());

            List<Product> results = productService.filterProduct(null, "900000-1000000");

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("UT020 Filter Returns Book In Range")
        void UT020_filterReturnsBookInRange() {
            // priceRange = '300000-400000'; sampleBook sellingPrice = 350000
            when(productRepository.findByPriceRange(300_000L, 400_000L))
                    .thenReturn(List.of(sampleBook));

            List<Product> results = productService.filterProduct(null, "300000-400000");

            assertThat(results).hasSize(1);
            assertThat(results.get(0)).isInstanceOf(Book.class);
        }

        @Test
        @DisplayName("UT021 Filter Returns DVD In Range")
        void UT021_filterReturnsDvdInRange() {
            // priceRange = '200000-260000'; sampleDvd sellingPrice = 250000
            when(productRepository.findByPriceRange(200_000L, 260_000L))
                    .thenReturn(List.of(sampleDvd));

            List<Product> results = productService.filterProduct(null, "200000-260000");

            assertThat(results).hasSize(1);
            assertThat(results.get(0)).isInstanceOf(DVD.class);
        }

        @Test
        @DisplayName("UT022 Filter Returns CD In Range")
        void UT022_filterReturnsCdInRange() {
            // priceRange = '150000-200000'; sampleCd sellingPrice = 180000
            when(productRepository.findByPriceRange(150_000L, 200_000L))
                    .thenReturn(List.of(sampleCd));

            List<Product> results = productService.filterProduct(null, "150000-200000");

            assertThat(results).hasSize(1);
            assertThat(results.get(0)).isInstanceOf(CD.class);
        }

        @Test
        @DisplayName("UT023 Filter Returns Newspaper In Range")
        void UT023_filterReturnsNewspaperInRange() {
            // priceRange = '10000-15000'; sampleNewspaper sellingPrice = 12000
            when(productRepository.findByPriceRange(10_000L, 15_000L))
                    .thenReturn(List.of(sampleNewspaper));

            List<Product> results = productService.filterProduct(null, "10000-15000");

            assertThat(results).hasSize(1);
            assertThat(results.get(0)).isInstanceOf(Newspaper.class);
        }

        @Test
        @DisplayName("UT024 Filter With Invalid Price Range Format")
        void UT024_filterWithInvalidPriceRangeFormat() {
            // priceRange = 'invalid' (no dash separator)
            assertThatThrownBy(() -> productService.filterProduct(null, "invalid"))
                    .isInstanceOf(InvalidProductInfoException.class)
                    .hasMessageContaining("price range");
        }

        @Test
        @DisplayName("UT025 Filter With Min Equals Max Boundary")
        void UT025_filterWithMinEqualsMaxBoundary() {
            // priceRange = '250000-250000'; min = max = 250000
            when(productRepository.findByPriceRange(250_000L, 250_000L))
                    .thenReturn(List.of(sampleDvd));

            List<Product> results = productService.filterProduct(null, "250000-250000");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getSellingPrice()).isEqualTo(250_000L);
        }

        @Test
        @DisplayName("UT026 Filter With Zero Min Price")
        void UT026_filterWithZeroMinPrice() {
            // priceRange = '0-500000'; min = 0 (lower boundary)
            when(productRepository.findByPriceRange(0L, 500_000L))
                    .thenReturn(List.of(sampleBook, sampleDvd, sampleCd, sampleNewspaper));

            List<Product> results = productService.filterProduct(null, "0-500000");

            assertThat(results).hasSize(4);
        }
    }
}

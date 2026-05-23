package com.aims.service;

import com.aims.entity.*;
import com.aims.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
Unit tests for UC: Search Product
Unit under test: ProductService.searchProduct(String keyword)

Equivalence Partitions:
 * EP1: keyword matches title → return list product with title match
 * EP2: keyword matches category → return list product with category match
 * EP3: keyword matches both title and category → return union (no duplicates)
 * EP4: keyword does not match anything → return empty list
 * EP5: keyword is empty string → return all products (LIKE '%%' match all)
 * EP6: keyword is null → return empty list (LIKE null = no match)
 * EP7: keyword uppercase → case-insensitive, still matches
 * EP8: keyword mixed case → case-insensitive, still matches
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProductServiceTest – UC: Search Product")
class SearchProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    private Book sampleBook;
    private DVD sampleDvd;
    private CD sampleCd;
    private Newspaper sampleNewspaper;

    @BeforeEach
    void setUp() {
        sampleBook = new Book(
                "Clean Code", "Book", "978-0132350884", "img.jpg",
                300_000L, 350_000L, 0.5, "Agile handbook", "24x16 cm", 50,
                "Prentice Hall", LocalDate.of(2008, 8, 1), "English",
                "Robert C. Martin", "Paperback", 431, "Technology");
        sampleBook.setProductId(1);

        sampleDvd = new DVD(
                "Inception", "DVD", "DVD-001", "inception.jpg",
                200_000L, 250_000L, 0.1, "Sci-fi thriller", "19x13 cm", 20,
                "Sci-Fi", LocalDate.of(2010, 7, 16),
                "Blu-ray", "Christopher Nolan", 148, "Warner Bros", "English", "Vietnamese");
        sampleDvd.setProductId(2);

        sampleCd = new CD(
                "Abbey Road", "CD", "CD-001", "abbey.jpg",
                150_000L, 180_000L, 0.08, "Classic album", "12x12 cm", 30,
                "Rock", LocalDate.of(1969, 9, 26),
                List.of("The Beatles"), "Apple Records");
        sampleCd.setProductId(3);

        sampleNewspaper = new Newspaper(
                "Tuoi Tre Daily", "Newspaper", "NEWS-001", "tuoitre.jpg",
                10_000L, 12_000L, 0.2, "Daily newspaper", "30x42 cm", 100,
                "Tuoi Tre", LocalDate.of(2024, 1, 1), "Vietnamese",
                "Nguyen Van A", "1234", "Daily", "ISSN-001",
                List.of("Politics", "Sports", "Economy"));
        sampleNewspaper.setProductId(4);
    }

    // ================================================================
    // EP1 – keyword matches title
    // ================================================================

    @Test
    @DisplayName("UT009 Search Keyword Matches Title")
    void UT009_searchKeywordMatchesTitle() {
        // EP1: keyword='Clean' matches title 'Clean Code'
        // Input : keyword='Clean'
        // Expect: Returns List size=1; type=Book; title='Clean Code'
        when(productRepository.searchByKeywordOrCategory("Clean"))
                .thenReturn(List.of(sampleBook));

        List<Product> results = productService.searchProduct("Clean");

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isInstanceOf(Book.class);
        assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("UT010 Search Keyword Matches Multiple Titles")
    void UT010_searchKeywordMatchesMultipleTitles() {
        // EP1: keyword='a' matches multiple titles
        // Input : keyword='a'
        // Expect: Returns List size=3
        when(productRepository.searchByKeywordOrCategory("a"))
                .thenReturn(List.of(sampleBook, sampleCd, sampleNewspaper));

        List<Product> results = productService.searchProduct("a");

        assertThat(results).hasSize(3);
        assertThat(results).extracting(Product::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Abbey Road", "Tuoi Tre Daily");
    }

    // ================================================================
    // EP2 – keyword matches category
    // ================================================================

    @Test
    @DisplayName("UT011 Search Keyword Matches Category DVD")
    void UT011_searchKeywordMatchesCategoryDvd() {
        // EP2: keyword='DVD' matches category='DVD'
        // Input : keyword='DVD'
        // Expect: Returns List size=1; type=DVD; category='DVD'
        when(productRepository.searchByKeywordOrCategory("DVD"))
                .thenReturn(List.of(sampleDvd));

        List<Product> results = productService.searchProduct("DVD");

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isInstanceOf(DVD.class);
        assertThat(results.get(0).getCategory()).isEqualTo("DVD");
    }

    @Test
    @DisplayName("UT012 Search Keyword Matches Category Book Returns Multiple")
    void UT012_searchKeywordMatchesCategoryBookReturnsMultiple() {
        // EP2: keyword='Book' matches category='Book' many products
        // Input : keyword='Book'
        // Expect: Returns List size=2; all category='Book'
        Book sampleBook2 = new Book(
                "The Pragmatic Programmer", "Book", "978-0135957059", "pragmatic.jpg",
                280_000L, 350_000L, 0.6, "Software craftsmanship", "23x15 cm", 30,
                "Addison-Wesley", LocalDate.of(2019, 9, 13), "English",
                "David Thomas", "Paperback", 352, "Technology");
        sampleBook2.setProductId(5);

        when(productRepository.searchByKeywordOrCategory("Book"))
                .thenReturn(List.of(sampleBook, sampleBook2));

        List<Product> results = productService.searchProduct("Book");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Product::getCategory).containsOnly("Book");
    }

    // ================================================================
    // EP3 – keyword matches both title and category → union, no duplicates
    // ================================================================

    @Test
    @DisplayName("UT013 Search Keyword Matches Both Title And Category")
    void UT013_searchKeywordMatchesBothTitleAndCategory() {
        // EP3: keyword='CD' matches title 'CD Mastering Guide' (category=Book) and matches category='CD' của sampleCd → SQL OR return union, no duplicates
        // Input : keyword='CD'
        // Expect: Returns List size=2; no duplicate productId
        Book cdBook = new Book(
                "CD Mastering Guide", "Book", "BOOK-CD-001", "cdbook.jpg",
                100_000L, 130_000L, 0.4, "Audio guide", "20x14 cm", 15,
                "Audio Press", LocalDate.of(2020, 1, 1), "English",
                "John Smith", "Paperback", 200, "Music");
        cdBook.setProductId(6);

        when(productRepository.searchByKeywordOrCategory("CD"))
                .thenReturn(List.of(sampleCd, cdBook));

        List<Product> results = productService.searchProduct("CD");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Product::getProductId).doesNotHaveDuplicates();
    }

    // ================================================================
    // EP4 – keyword no match → return empty list
    // ================================================================

    @Test
    @DisplayName("UT014 Search Keyword No Match Returns Empty List")
    void UT014_searchKeywordNoMatchReturnsEmptyList() {
        // EP4: keyword='XYZ' doesn't match any title or category
        // Input : keyword='XYZ'
        // Expect: Returns empty List
        when(productRepository.searchByKeywordOrCategory("XYZ"))
                .thenReturn(List.of());

        List<Product> results = productService.searchProduct("XYZ");

        assertThat(results).isEmpty();
    }

    // ================================================================
    // EP5 – keyword empty → LIKE '%%' match all
    // ================================================================

    @Test
    @DisplayName("UT015 Search Empty Keyword Returns All Products")
    void UT015_searchEmptyKeywordReturnsAllProducts() {
        // EP5: keyword='' → SQL LIKE '%%' match all
        // Input : keyword=''
        // Expect: Returns List size=4; contains all 4 product types
        when(productRepository.searchByKeywordOrCategory(""))
                .thenReturn(List.of(sampleBook, sampleDvd, sampleCd, sampleNewspaper));

        List<Product> results = productService.searchProduct("");

        assertThat(results).hasSize(4);
        assertThat(results).extracting(Product::getCategory)
                .containsExactlyInAnyOrder("Book", "DVD", "CD", "Newspaper");
    }

    // ================================================================
    // EP6 – keyword null → LIKE null = no match → empty list
    // ================================================================

    @Test
    @DisplayName("UT016 Search Null Keyword Returns Empty List")
    void UT016_searchNullKeywordReturnsEmptyList() {
        // EP6: keyword=null → SQL CONCAT('%',null,'%')=null → LIKE null = no match
        // Input : keyword=null
        // Expect: Returns empty List
        when(productRepository.searchByKeywordOrCategory(null))
                .thenReturn(List.of());

        List<Product> results = productService.searchProduct(null);

        assertThat(results).isEmpty();
    }

    // ================================================================
    // EP7 – keyword uppercase → case-insensitive, still matches
    // ================================================================

    @Test
    @DisplayName("UT017 Search Uppercase Keyword Matches Title")
    void UT017_searchUppercaseKeywordMatchesTitle() {
        // EP7: keyword='CLEAN' → SQL LOWER('CLEAN')='clean' matches LOWER('Clean Code')
        // Input : keyword='CLEAN'
        // Expect: Returns List size=1; title='Clean Code'
        when(productRepository.searchByKeywordOrCategory("CLEAN"))
                .thenReturn(List.of(sampleBook));

        List<Product> results = productService.searchProduct("CLEAN");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("UT018 Search Uppercase Keyword Matches Category")
    void UT018_searchUppercaseKeywordMatchesCategory() {
        // EP7: keyword='NEWSPAPER' → SQL LOWER matches category='Newspaper'
        // Input : keyword='NEWSPAPER'
        // Expect: Returns List size=1; category='Newspaper'
        when(productRepository.searchByKeywordOrCategory("NEWSPAPER"))
                .thenReturn(List.of(sampleNewspaper));

        List<Product> results = productService.searchProduct("NEWSPAPER");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCategory()).isEqualTo("Newspaper");
    }

    // ================================================================
    // EP8 – keyword mixed case → case-insensitive, still matches
    // ================================================================

    @Test
    @DisplayName("UT019 Search Mixed Case Keyword Matches Title")
    void UT019_searchMixedCaseKeywordMatchesTitle() {
        // EP8: keyword='cLeAn' → SQL LOWER('cLeAn')='clean' matches LOWER('Clean Code')
        // Input : keyword='cLeAn'
        // Expect: Returns List size=1; title='Clean Code'
        when(productRepository.searchByKeywordOrCategory("cLeAn"))
                .thenReturn(List.of(sampleBook));

        List<Product> results = productService.searchProduct("cLeAn");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("UT020 Search Mixed Case Keyword Matches Category")
    void UT020_searchMixedCaseKeywordMatchesCategory() {
        // EP8: keyword='dVd' → SQL LOWER('dVd')='dvd' matches LOWER('DVD')
        // Input : keyword='dVd'
        // Expect: Returns List size=1; category='DVD'
        when(productRepository.searchByKeywordOrCategory("dVd"))
                .thenReturn(List.of(sampleDvd));

        List<Product> results = productService.searchProduct("dVd");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCategory()).isEqualTo("DVD");
    }
}
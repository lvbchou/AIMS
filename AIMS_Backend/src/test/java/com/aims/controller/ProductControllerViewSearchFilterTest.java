package com.aims.controller;

import com.aims.entity.*;
import com.aims.exception.InvalidProductInfoException;
import com.aims.exception.ProductNotFoundException;
import com.aims.service.ProductService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer unit tests for UC: View Product Details, Search Product, Filter Product
 * Strategy: Black-box – Equivalence Partitioning (EP) + Boundary Value Analysis (BVA)
 *
 * CT001–CT007  GET /api/products/{barcode}   (View Product)
 * CT008–CT014  GET /api/products/search       (Search Product)
 * CT015–CT020  GET /api/products/filter       (Filter Product)
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController – View Product / Search Product / Filter Product")
class ProductControllerViewSearchFilterTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private ProductService productService;

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

        sampleDvd = new DVD(
                "Inception", "Film", "DVD-001", "inception.jpg",
                200_000L, 250_000L, 0.1, "Sci-fi thriller", "19x13 cm", 20,
                "Sci-Fi", LocalDate.of(2010, 7, 16),
                "Blu-ray", "Christopher Nolan", 148, "Warner Bros", "English", "Vietnamese");

        sampleCd = new CD(
                "Abbey Road", "Music", "CD-001", "abbey.jpg",
                150_000L, 180_000L, 0.08, "Classic album", "12x12 cm", 30,
                "Rock", LocalDate.of(1969, 9, 26),
                List.of("The Beatles"), "Apple Records");

        sampleNewspaper = new Newspaper(
                "Tuoi Tre Daily", "News", "NEWS-001", "tuoitre.jpg",
                10_000L, 12_000L, 0.2, "Daily newspaper", "30x42 cm", 100,
                "Tuoi Tre", LocalDate.of(2024, 1, 1), "Vietnamese",
                "Nguyen Van A", "1234", "Daily", "ISSN-001",
                List.of("Politics", "Sports", "Economy"));
    }

    // ================================================================
    // GET /api/products/{barcode} – View Product Details
    // ================================================================

    @Nested
    @DisplayName("GET /api/products/{barcode}")
    class ViewProductEndpoint {

        @Test
        @DisplayName("CT001 View Book Product Returns 200")
        void CT001_viewBookProductReturns200() throws Exception {
            // GET /api/products/978-0132350884; product type = Book
            when(productService.viewProduct("978-0132350884")).thenReturn(sampleBook);

            mockMvc.perform(get("/api/products/978-0132350884").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.barcode").value("978-0132350884"))
                    .andExpect(jsonPath("$.title").value("Clean Code"));
        }

        @Test
        @DisplayName("CT002 View DVD Product Returns 200")
        void CT002_viewDvdProductReturns200() throws Exception {
            // GET /api/products/DVD-001; product type = DVD
            when(productService.viewProduct("DVD-001")).thenReturn(sampleDvd);

            mockMvc.perform(get("/api/products/DVD-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Inception"));
        }

        @Test
        @DisplayName("CT003 View CD Product Returns 200")
        void CT003_viewCdProductReturns200() throws Exception {
            // GET /api/products/CD-001; product type = CD
            when(productService.viewProduct("CD-001")).thenReturn(sampleCd);

            mockMvc.perform(get("/api/products/CD-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Abbey Road"));
        }

        @Test
        @DisplayName("CT004 View Newspaper Product Returns 200")
        void CT004_viewNewspaperProductReturns200() throws Exception {
            // GET /api/products/NEWS-001; product type = Newspaper
            when(productService.viewProduct("NEWS-001")).thenReturn(sampleNewspaper);

            mockMvc.perform(get("/api/products/NEWS-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Tuoi Tre Daily"));
        }

        @Test
        @DisplayName("CT005 View Product With Nonexistent Barcode Returns 404")
        void CT005_viewProductWithNonexistentBarcodeReturns404() throws Exception {
            // GET /api/products/nonexistent-999; service throws ProductNotFoundException
            when(productService.viewProduct("nonexistent-999"))
                    .thenThrow(new ProductNotFoundException("nonexistent-999"));

            mockMvc.perform(get("/api/products/nonexistent-999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CT006 View Product With Unknown Barcode Returns 404")
        void CT006_viewProductWithUnknownBarcodeReturns404() throws Exception {
            // GET /api/products/some-barcode; any unknown barcode returns 404
            when(productService.viewProduct(anyString()))
                    .thenThrow(new ProductNotFoundException("some-barcode"));

            mockMvc.perform(get("/api/products/some-barcode"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CT007 View Product Calls Service Once")
        void CT007_viewProductCallsServiceOnce() throws Exception {
            // GET /api/products/978-0132350884; verify service delegation
            when(productService.viewProduct("978-0132350884")).thenReturn(sampleBook);

            mockMvc.perform(get("/api/products/978-0132350884"));

            verify(productService, times(1)).viewProduct("978-0132350884");
        }
    }

    // ================================================================
    // GET /api/products/search – Search Product
    // ================================================================

    @Nested
    @DisplayName("GET /api/products/search")
    class SearchProductEndpoint {

        @Test
        @DisplayName("CT008 Search By Keyword And Category Programming Returns 200")
        void CT008_searchByKeywordAndCategoryProgrammingReturns200() throws Exception {
            // GET /api/products/search?keyword=Clean&category=Programming
            when(productService.searchProduct("Clean", "Programming")).thenReturn(List.of(sampleBook));

            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "Clean")
                            .param("category", "Programming"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Clean Code"));
        }

        @Test
        @DisplayName("CT009 Search By Keyword And Category Film Returns 200")
        void CT009_searchByKeywordAndCategoryFilmReturns200() throws Exception {
            // GET /api/products/search?keyword=Inception&category=Film
            when(productService.searchProduct("Inception", "Film")).thenReturn(List.of(sampleDvd));

            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "Inception")
                            .param("category", "Film"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Inception"));
        }

        @Test
        @DisplayName("CT010 Search By Keyword And Category Music Returns 200")
        void CT010_searchByKeywordAndCategoryMusicReturns200() throws Exception {
            // GET /api/products/search?keyword=Abbey&category=Music
            when(productService.searchProduct("Abbey", "Music")).thenReturn(List.of(sampleCd));

            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "Abbey")
                            .param("category", "Music"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Abbey Road"));
        }

        @Test
        @DisplayName("CT011 Search By Keyword And Category News Returns 200")
        void CT011_searchByKeywordAndCategoryNewsReturns200() throws Exception {
            // GET /api/products/search?keyword=Tuoi+Tre&category=News
            when(productService.searchProduct("Tuoi Tre", "News")).thenReturn(List.of(sampleNewspaper));

            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "Tuoi Tre")
                            .param("category", "News"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Tuoi Tre Daily"));
        }

        @Test
        @DisplayName("CT012 Search With No Match Returns 200 Empty List")
        void CT012_searchWithNoMatchReturns200EmptyList() throws Exception {
            // GET /api/products/search?keyword=xyz123&category=unknown; no match → 200 []
            when(productService.searchProduct("xyz123", "unknown")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "xyz123")
                            .param("category", "unknown"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("CT013 Search By Keyword Only Returns 200")
        void CT013_searchByKeywordOnlyReturns200() throws Exception {
            // GET /api/products/search?keyword=Clean; no category param
            when(productService.searchProduct("Clean", null)).thenReturn(List.of(sampleBook));

            mockMvc.perform(get("/api/products/search").param("keyword", "Clean"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Clean Code"));
        }

        @Test
        @DisplayName("CT014 Search By Category Only Returns 200")
        void CT014_searchByCategoryOnlyReturns200() throws Exception {
            // GET /api/products/search?category=Music; no keyword param
            when(productService.searchProduct(null, "Music")).thenReturn(List.of(sampleCd));

            mockMvc.perform(get("/api/products/search").param("category", "Music"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Abbey Road"));
        }
    }

    // ================================================================
    // GET /api/products/filter – Filter Product
    // ================================================================

    @Nested
    @DisplayName("GET /api/products/filter")
    class FilterProductEndpoint {

        @Test
        @DisplayName("CT015 Filter Products In Valid Price Range Returns 200")
        void CT015_filterProductsInValidPriceRangeReturns200() throws Exception {
            // GET /api/products/filter?priceRange=100000-500000
            when(productService.filterProduct(isNull(), eq("100000-500000")))
                    .thenReturn(List.of(sampleBook, sampleDvd, sampleCd));

            mockMvc.perform(get("/api/products/filter").param("priceRange", "100000-500000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        @DisplayName("CT016 Filter With No Products In Range Returns 200")
        void CT016_filterWithNoProductsInRangeReturns200() throws Exception {
            // GET /api/products/filter?priceRange=9000000-10000000; no products → 200 []
            when(productService.filterProduct(isNull(), eq("9000000-10000000")))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/products/filter").param("priceRange", "9000000-10000000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("CT017 Filter With Invalid Price Range Returns 400")
        void CT017_filterWithInvalidPriceRangeReturns400() throws Exception {
            // GET /api/products/filter?priceRange=invalid; service throws InvalidProductInfoException
            when(productService.filterProduct(isNull(), eq("invalid")))
                    .thenThrow(new InvalidProductInfoException("Invalid price range format. Expected: min-max"));

            mockMvc.perform(get("/api/products/filter").param("priceRange", "invalid"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("CT018 Filter With Missing Price Range Param Returns 400")
        void CT018_filterWithMissingPriceRangeParamReturns400() throws Exception {
            // GET /api/products/filter; priceRange param absent → Spring 400
            mockMvc.perform(get("/api/products/filter"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("CT019 Filter With Min Equals Max Boundary Returns 200")
        void CT019_filterWithMinEqualsMaxBoundaryReturns200() throws Exception {
            // GET /api/products/filter?priceRange=250000-250000; min = max = 250000
            when(productService.filterProduct(isNull(), eq("250000-250000")))
                    .thenReturn(List.of(sampleDvd));

            mockMvc.perform(get("/api/products/filter").param("priceRange", "250000-250000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("CT020 Filter With Zero Min Price Returns 200")
        void CT020_filterWithZeroMinPriceReturns200() throws Exception {
            // GET /api/products/filter?priceRange=0-500000; min = 0 (lower boundary)
            when(productService.filterProduct(isNull(), eq("0-500000")))
                    .thenReturn(List.of(sampleBook, sampleDvd, sampleCd, sampleNewspaper));

            mockMvc.perform(get("/api/products/filter").param("priceRange", "0-500000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(4));
        }
    }
}

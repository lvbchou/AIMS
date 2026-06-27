package com.aims.controller.product;

import com.aims.dto.product.ProductSummaryDTO;
import com.aims.exception.EmptySearchInputException;
import com.aims.exception.GlobalExceptionHandler;
import com.aims.exception.InvalidProductInfoException;
import com.aims.service.product.IProductSearchService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer tests cho UC SearchProduct + FilterProduct.
 * Endpoint: GET /api/products/search (search), và search?priceRange=... (filter).
 *
 * Dùng standalone MockMvc (không load Spring context) -> nhanh, không vướng
 * Spring Security / springdoc. GlobalExceptionHandler được gắn để map
 * EmptySearchInputException / InvalidProductInfoException -> 400.
 *
 *   CT008–CT014  Search
 *   CT015–CT020  Filter
 */
@DisplayName("SearchController – Search / Filter")
class SearchControllerTest {

    @Mock private IProductSearchService productSearchService;

    private MockMvc mockMvc;
    private AutoCloseable mocks;

    private ProductSummaryDTO bookSummary;
    private ProductSummaryDTO dvdSummary;
    private ProductSummaryDTO cdSummary;
    private ProductSummaryDTO newspaperSummary;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        ProductSearchController controller = new ProductSearchController(productSearchService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        bookSummary      = new ProductSummaryDTO(1, "Clean Code",     "BOOK",      350_000L, "img.jpg",       50);
        dvdSummary       = new ProductSummaryDTO(2, "Inception",      "DVD",       250_000L, "inception.jpg", 20);
        cdSummary        = new ProductSummaryDTO(3, "Abbey Road",     "CD",        180_000L, "abbey.jpg",     30);
        newspaperSummary = new ProductSummaryDTO(4, "Tuoi Tre Daily", "NEWSPAPER", 12_000L,  "tuoitre.jpg",  100);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    // ================================================================
    // GET /api/products/search – Search Product
    // ================================================================
    @Nested
    @DisplayName("GET /api/products/search")
    class SearchEndpoint {

        @Test
        @DisplayName("CT008 Search By Keyword Returns 200")
        void CT008_searchByKeywordReturns200() throws Exception {
            when(productSearchService.searchProduct(eq("Clean"), isNull(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(bookSummary)));

            mockMvc.perform(get("/api/products/search").param("keyword", "Clean"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
        }

        @Test
        @DisplayName("CT009 Search By Category Returns 200")
        void CT009_searchByCategoryReturns200() throws Exception {
            when(productSearchService.searchProduct(isNull(), eq("Film"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(dvdSummary)));

            mockMvc.perform(get("/api/products/search").param("category", "Film"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Inception"));
        }

        @Test
        @DisplayName("CT010 Search By Keyword And Category Returns 200")
        void CT010_searchByKeywordAndCategoryReturns200() throws Exception {
            when(productSearchService.searchProduct(eq("Abbey"), eq("Music"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(cdSummary)));

            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "Abbey")
                            .param("category", "Music"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Abbey Road"));
        }

        @Test
        @DisplayName("CT011 Search News Category Returns 200")
        void CT011_searchNewsCategoryReturns200() throws Exception {
            when(productSearchService.searchProduct(eq("Tuoi Tre"), eq("News"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(newspaperSummary)));

            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "Tuoi Tre")
                            .param("category", "News"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Tuoi Tre Daily"));
        }

        @Test
        @DisplayName("CT012 Search With No Match Returns 200 Empty Page")
        void CT012_searchWithNoMatchReturns200EmptyPage() throws Exception {
            when(productSearchService.searchProduct(eq("xyz123"), isNull(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/products/search").param("keyword", "xyz123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        @DisplayName("CT013 Search With Empty Input Returns 400")
        void CT013_searchWithEmptyInputReturns400() throws Exception {
            // Alternative Flow: cả keyword và category rỗng -> EmptySearchInputException -> 400
            when(productSearchService.searchProduct(isNull(), isNull(), any(Pageable.class)))
                    .thenThrow(new EmptySearchInputException());

            mockMvc.perform(get("/api/products/search"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Please enter a product title or category"));
        }

        @Test
        @DisplayName("CT014 Search Calls Service Once")
        void CT014_searchCallsServiceOnce() throws Exception {
            when(productSearchService.searchProduct(eq("Clean"), isNull(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(bookSummary)));

            mockMvc.perform(get("/api/products/search").param("keyword", "Clean"));

            verify(productSearchService, times(1))
                    .searchProduct(eq("Clean"), isNull(), any(Pageable.class));
        }
    }

    // ================================================================
    // GET /api/products/search?priceRange=... – Filter Product
    // ================================================================
    @Nested
    @DisplayName("GET /api/products/search (filter by priceRange)")
    class FilterEndpoint {

        @Test
        @DisplayName("CT015 Filter In Valid Price Range Returns 200")
        void CT015_filterInValidPriceRangeReturns200() throws Exception {
            when(productSearchService.filterProductsByPriceRange(
                    isNull(), eq("Programming"), eq("100000-500000"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(bookSummary)));

            mockMvc.perform(get("/api/products/search")
                            .param("category", "Programming")
                            .param("priceRange", "100000-500000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
        }

        @Test
        @DisplayName("CT016 Filter With No Product In Range Returns 200 Empty")
        void CT016_filterWithNoProductInRangeReturns200Empty() throws Exception {
            when(productSearchService.filterProductsByPriceRange(
                    isNull(), eq("Music"), eq("9000000-10000000"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/products/search")
                            .param("category", "Music")
                            .param("priceRange", "9000000-10000000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        @DisplayName("CT017 Filter With Invalid Price Range Returns 400")
        void CT017_filterWithInvalidPriceRangeReturns400() throws Exception {
            when(productSearchService.filterProductsByPriceRange(
                    isNull(), eq("Programming"), eq("invalid"), any(Pageable.class)))
                    .thenThrow(new InvalidProductInfoException("Invalid price range format."));

            mockMvc.perform(get("/api/products/search")
                            .param("category", "Programming")
                            .param("priceRange", "invalid"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("CT018 Filter With Empty Input Returns 400")
        void CT018_filterWithEmptyInputReturns400() throws Exception {
            when(productSearchService.filterProductsByPriceRange(
                    isNull(), isNull(), eq("100000-500000"), any(Pageable.class)))
                    .thenThrow(new EmptySearchInputException());

            mockMvc.perform(get("/api/products/search").param("priceRange", "100000-500000"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("CT019 Filter With Keyword And Price Range Returns 200")
        void CT019_filterWithKeywordAndPriceRangeReturns200() throws Exception {
            when(productSearchService.filterProductsByPriceRange(
                    eq("Clean"), isNull(), eq("300000-400000"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(bookSummary)));

            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "Clean")
                            .param("priceRange", "300000-400000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
        }

        @Test
        @DisplayName("CT020 Filter Calls Filter Service (Not Search) When priceRange Present")
        void CT020_filterCallsFilterServiceWhenPriceRangePresent() throws Exception {
            when(productSearchService.filterProductsByPriceRange(
                    isNull(), eq("Programming"), eq("0-500000"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(bookSummary)));

            mockMvc.perform(get("/api/products/search")
                            .param("category", "Programming")
                            .param("priceRange", "0-500000"));

            verify(productSearchService, times(1)).filterProductsByPriceRange(
                    isNull(), eq("Programming"), eq("0-500000"), any(Pageable.class));
            verify(productSearchService, never()).searchProduct(any(), any(), any());
        }
    }
}
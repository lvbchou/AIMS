package com.aims.controller.product;

import com.aims.dto.product.BookInfoDTO;
import com.aims.dto.product.ProductInfoDTO;
import com.aims.exception.GlobalExceptionHandler;
import com.aims.exception.ProductNotFoundException;
import com.aims.service.product.IProductQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer tests cho UC ViewProductDetails.
 * Endpoint: GET /api/products/{productId}  (productId là Integer).
 *
 * Dùng standalone MockMvc (không load Spring context) -> nhanh, không vướng
 * Spring Security / springdoc. Vẫn kiểm tra đúng routing, status, JSON và
 * GlobalExceptionHandler (map ProductNotFoundException -> 404).
 *
 *   CT001–CT007
 */
@DisplayName("ViewController – View Product Details")
class ViewControllerTest {

    @Mock private IProductQueryService productQueryService;

    private MockMvc mockMvc;
    private ProductInfoDTO bookInfo;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        ProductQueryController controller = new ProductQueryController(productQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        bookInfo = new BookInfoDTO();
        bookInfo.setProductId(1);
        bookInfo.setProductType("BOOK");
        bookInfo.setTitle("Clean Code");
        bookInfo.setBarcode("978-0132350884");
        bookInfo.setSellingPrice(350_000L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("CT001 View Valid Product Returns 200")
    void CT001_viewValidProductReturns200() throws Exception {
        when(productQueryService.viewProduct(1)).thenReturn(bookInfo);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("CT002 View Valid Product Returns Correct ProductType")
    void CT002_viewValidProductReturnsCorrectType() throws Exception {
        when(productQueryService.viewProduct(1)).thenReturn(bookInfo);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productType").value("BOOK"));
    }

    @Test
    @DisplayName("CT003 View Valid Product Returns Barcode")
    void CT003_viewValidProductReturnsBarcode() throws Exception {
        when(productQueryService.viewProduct(1)).thenReturn(bookInfo);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcode").value("978-0132350884"));
    }

    @Test
    @DisplayName("CT004 View Product With Nonexistent Id Returns 404")
    void CT004_viewProductWithNonexistentIdReturns404() throws Exception {
        when(productQueryService.viewProduct(999))
                .thenThrow(new ProductNotFoundException(999));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CT005 View Product With Unknown Id Returns 404 With Message")
    void CT005_viewProductWithUnknownIdReturns404WithMessage() throws Exception {
        when(productQueryService.viewProduct(anyInt()))
                .thenThrow(new ProductNotFoundException(12345));

        mockMvc.perform(get("/api/products/12345"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("12345")));
    }

    @Test
    @DisplayName("CT006 View Product With Non-Numeric Id Is Not 200")
    void CT006_viewProductWithNonNumericIdIsNotOk() throws Exception {
        // path variable là Integer; truyền chữ -> type mismatch -> KHÔNG phải 200,
        // và service không bao giờ được gọi.
        int status = mockMvc.perform(get("/api/products/abc"))
                .andReturn().getResponse().getStatus();

        org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(200);
        verify(productQueryService, never()).viewProduct(anyInt());
    }

    @Test
    @DisplayName("CT007 View Product Calls Service Once")
    void CT007_viewProductCallsServiceOnce() throws Exception {
        when(productQueryService.viewProduct(1)).thenReturn(bookInfo);

        mockMvc.perform(get("/api/products/1"));

        verify(productQueryService, times(1)).viewProduct(1);
    }
}
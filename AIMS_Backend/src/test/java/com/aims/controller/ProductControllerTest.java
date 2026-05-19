package com.aims.controller;

import com.aims.dto.ProductInfoDTO;
import com.aims.entity.Book;
import com.aims.exception.ProductNotFoundException;
import com.aims.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*; import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType; import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate; import java.util.List;
import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private ProductService productService;
    @Autowired private ObjectMapper objectMapper;
    private ProductInfoDTO validBookDTO;

    @BeforeEach void setUp(){
        validBookDTO=ProductInfoDTO.builder().productType("BOOK").title("Clean Code")
                .category("Programming").barcode("978-0132350884").originalValue(300_000L)
                .sellingPrice(350_000L).weight(0.5).quantityInStock(50).publisher("Prentice Hall")
                .publicationDate(LocalDate.of(2008,8,1)).language("English")
                .author("Robert C. Martin").coverType("Paperback").pages(431).genre("Technology").build();
    }

    @Test @DisplayName("POST /api/products - 201 on successful create")
    void createProduct_shouldReturn201() throws Exception {
        doNothing().when(productService).saveProduct(any(ProductInfoDTO.class));
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBookDTO))).andExpect(status().isCreated());
    }
    @Test @DisplayName("GET /api/products/{barcode} - 200 with product")
    void viewProduct_shouldReturn200() throws Exception {
        Book book=new Book(); book.setBarcode(validBookDTO.getBarcode()); book.setTitle(validBookDTO.getTitle());
        when(productService.viewProduct(validBookDTO.getBarcode())).thenReturn(book);
        mockMvc.perform(get("/api/products/"+validBookDTO.getBarcode())).andExpect(status().isOk());
    }
    @Test @DisplayName("GET /api/products/{barcode} - 404 when not found")
    void viewProduct_shouldReturn404() throws Exception {
        when(productService.viewProduct(anyString())).thenThrow(new ProductNotFoundException("nonexistent"));
        mockMvc.perform(get("/api/products/nonexistent")).andExpect(status().isNotFound());
    }
    @Test @DisplayName("DELETE /api/products/{productId} - 204")
    void deleteProduct_shouldReturn204() throws Exception {
        Book book=new Book(); book.setProductId(1);
        when(productService.viewProduct("1")).thenReturn(book);
        doNothing().when(productService).deleteProduct(any());
        mockMvc.perform(delete("/api/products/1")).andExpect(status().isNoContent());
    }
    @Test @DisplayName("GET /api/products/search - 200 with list")
    void searchProduct_shouldReturn200() throws Exception {
        Book book=new Book(); book.setTitle("Clean Code");
        when(productService.searchProduct("Clean","Programming")).thenReturn(List.of(book));
        mockMvc.perform(get("/api/products/search").param("keyword","Clean").param("category","Programming"))
                .andExpect(status().isOk());
    }
    @Test @DisplayName("GET /api/products/filter - 200 with filtered products")
    void filterProduct_shouldReturn200() throws Exception {
        Book book=new Book(); book.setSellingPrice(200_000L);
        when(productService.filterProduct(any(),anyString())).thenReturn(List.of(book));
        mockMvc.perform(get("/api/products/filter").param("priceRange","100000-500000")).andExpect(status().isOk());
    }
}

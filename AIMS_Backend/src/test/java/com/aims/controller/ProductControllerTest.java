package com.aims.controller;

import com.aims.dto.ProductInfoDTO;
import com.aims.exception.ProductNotFoundException;
import com.aims.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private final ObjectMapper jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private ProductInfoDTO validBookDTO;

    @BeforeEach
    void setUp() {
        validBookDTO = ProductInfoDTO.builder()
                .productType("BOOK")
                .title("Clean Code")
                .category("Programming")
                .barcode("978-0132350884")
                .originalValue(300_000L)
                .sellingPrice(350_000L)
                .weight(0.5)
                .quantityInStock(50)
                .publisher("Prentice Hall")
                .publicationDate(LocalDate.of(2008, 8, 1))
                .language("English")
                .author("Robert C. Martin")
                .coverType("Paperback")
                .pages(431)
                .genre("Technology")
                .build();
    }

    @Test
    @DisplayName("POST /api/products - should return 201 on successful create")
    void createProduct_shouldReturn201() throws Exception {
        Book savedBook = new Book();
        savedBook.setProductId(1);
        savedBook.setBarcode(validBookDTO.getBarcode());
        savedBook.setTitle(validBookDTO.getTitle());
        when(productService.saveProduct(any(ProductInfoDTO.class))).thenReturn(savedBook);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(validBookDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.barcode").value(validBookDTO.getBarcode()));
    }

    @Test
    @DisplayName("DELETE /api/products/{productId} - should return 204")
    void deleteProduct_shouldReturn204() throws Exception {
        Book book = new Book();
        book.setProductId(1);
        when(productService.viewProduct("1")).thenReturn(book);
        doNothing().when(productService).deleteProduct(any());

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}

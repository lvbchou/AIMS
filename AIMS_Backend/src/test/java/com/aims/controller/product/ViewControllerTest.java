package com.aims.controller.product;

import com.aims.dto.product.BookInfoDTO;
import com.aims.exception.GlobalExceptionHandler;
import com.aims.service.product.IProductQueryService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Controller-layer test cho UC ViewProductDetails.
 * Endpoint: GET /api/products/{productId}
 *
 * UT005 trong tai lieu ("product_id khong phai so nguyen") thuoc TANG NAY:
 *   path variable kieu Integer -> truyen '3.14' hoac 'abc' gay type mismatch
 *   -> KHONG phai 200, va service khong bao gio duoc goi.
 *   (Service nhan Integer nen khong the test non-integer o tang service.)
 */
@DisplayName("UC ViewProductDetails - ViewController (UT005)")
class ViewControllerTest {

    @Mock private IProductQueryService productQueryService;

    private MockMvc mockMvc;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        ProductQueryController controller = new ProductQueryController(productQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    // UT005 - non-integer product_id bi tu choi o tang controller
    @Test
    @DisplayName("UT005 Non-integer id is not 200 and service is never called")
    void UT005_viewNonIntegerIdIsNotOk() throws Exception {
        // '3.14' khong parse duoc thanh Integer -> type mismatch -> khong 200.
        int statusDecimal = mockMvc.perform(get("/api/products/3.14"))
                .andReturn().getResponse().getStatus();
        assertThat(statusDecimal).isNotEqualTo(200);

        // 'abc' tuong tu.
        int statusAlpha = mockMvc.perform(get("/api/products/abc"))
                .andReturn().getResponse().getStatus();
        assertThat(statusAlpha).isNotEqualTo(200);

        verify(productQueryService, never()).viewProduct(anyInt());
    }
}
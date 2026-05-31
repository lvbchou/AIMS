package com.aims.controller;

import com.aims.dto.*;
import com.aims.service.PayOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayOrderController.class)
class PayOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

        @MockBean
    private PayOrderService payOrderService;

        private final ObjectMapper jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("GET /api/orders/{orderId}/pay/invoice returns invoice projection")
    void getInvoice_returns200() throws Exception {
        InvoiceScreenDTO dto = InvoiceScreenDTO.builder()
                .orderId("ORD-001")
                .invoiceId("INV-001")
                .lineItems(List.of(
                        InvoiceLineItemDTO.builder()
                                .productTitle("DVD Mai")
                                .quantity(2)
                                .unitSellingPrice(18_000)
                                .lineTotalSellingPrice(36_000)
                                .build()))
                .totalProductPriceExclVat(360_000)
                .totalProductPriceInclVat(400_000)
                .deliveryFee(30_000)
                .totalAmountToBePaid(430_000)
                .build();

        when(payOrderService.getInvoiceScreen("ORD-001")).thenReturn(dto);

        mockMvc.perform(get("/api/orders/ORD-001/pay/invoice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value("INV-001"))
                .andExpect(jsonPath("$.totalAmountToBePaid").value(430_000));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/pay/vietqr/qrcode returns QR bundle")
    void requestQr_returns200() throws Exception {
        VietQRCodeResponseDTO dto = VietQRCodeResponseDTO.builder()
                .orderId("ORD-001")
                .invoiceId("INV-001")
                .transactionId("TX-123")
                .vietQrReference("VIETQR-REF")
                .qrCodeImageBase64("AAA")
                .totalAmountToBePaid(430_000)
                .build();

        when(payOrderService.requestVietQrDisplay("ORD-001")).thenReturn(dto);

        mockMvc.perform(post("/api/orders/ORD-001/pay/vietqr/qrcode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TX-123"));
    }

    @Test
    @DisplayName("POST /api/payments/vietqr/callback returns 204")
    void callback_returns204() throws Exception {
        doNothing().when(payOrderService).handleVietQrPaymentCallback(any());

        VietQrCallbackRequestDTO body = VietQrCallbackRequestDTO.builder()
                .transactionId("TX-123")
                .paymentStatus("SUCCESS")
                .build();

        mockMvc.perform(post("/api/payments/vietqr/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/orders/{orderId}/pay/confirmation returns summary")
    void confirmation_returns200() throws Exception {
        OrderConfirmationDTO dto = OrderConfirmationDTO.builder()
                .customerName("Le Trong Viet Dung")
                .phoneNumber("0123456789")
                .shippingAddress("1 Hue Street")
                .province("Hanoi")
                .totalAmountToBePaid(1_900_000)
                .transactionId("11111")
                .transactionContent("Order #00001")
                .transactionDatetimeDisplay("16/03/2026 19:00:00")
                .build();

        when(payOrderService.getOrderConfirmation(eq("ORD-001"))).thenReturn(dto);

        mockMvc.perform(get("/api/orders/ORD-001/pay/confirmation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("11111"));
    }
}

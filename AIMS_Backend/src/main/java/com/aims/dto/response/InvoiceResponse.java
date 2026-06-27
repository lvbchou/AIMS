package com.aims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private String invoiceId;
    private String orderId;
    private LocalDateTime issueDate;
    @Builder.Default
    private List<InvoiceLineResponse> items = new ArrayList<>();
    private Long subtotalExVAT;
    private Long vat;
    private Long subtotalIncVAT;
    private Long shippingFee;
    private Long total;
}

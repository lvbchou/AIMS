package com.aims.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineResponse {
    private Integer productId;
    private String title;
    private String category;
    private String image;
    private Integer quantity;
    private Long unitPriceExVat;
    private Long amountExVat;
}

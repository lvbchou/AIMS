package com.aims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UC003 Table 2 — per-line invoice presentation (media rows).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineItemDTO {

    private String productTitle;
    private int quantity;
    private long unitSellingPrice;
    private long lineTotalSellingPrice;
}

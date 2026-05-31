package com.aims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This DTO aggregates the invoice values and line items needed by the payment
 * screen.
 *
 * @author Team 03
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceScreenDTO {

    private String orderId;
    private String invoiceId;
    private List<InvoiceLineItemDTO> lineItems;
    private long totalProductPriceExclVat;
    private long totalProductPriceInclVat;
    private long deliveryFee;
    private long totalAmountToBePaid;
}

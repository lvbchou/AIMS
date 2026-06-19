package com.aims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailabilityIssue {
    private Integer productId;
    private String title;
    private Integer requestedQuantity;
    private Integer availableQuantity;
}

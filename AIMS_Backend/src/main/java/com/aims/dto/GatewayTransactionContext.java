package com.aims.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GatewayTransactionContext - DTO holding the context of a payment session initiated with a gateway.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GatewayTransactionContext {

    private String paypalOrderId;
    private String approveUrl;

    /**
     * Alias method to satisfy getApprovalUrl() calls in the business logic.
     */
    public String getApprovalUrl() {
        return approveUrl;
    }
}

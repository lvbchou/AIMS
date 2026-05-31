package com.aims.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GatewayTransactionResult - DTO holding the final result of completing/capturing a transaction.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GatewayTransactionResult {

    private String transactionId;
    private String orderId;
    private String status;
    private boolean success;
    private String message;

    /**
     * Helper method to satisfy checkSuccess() calls in the services layer.
     */
    public boolean checkSuccess() {
        return this.success;
    }
}

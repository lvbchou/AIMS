/**
 * SOLID Principles Analysis:
 * - **SRP/OCP Adherence**: Correctly acts as a generic, vendor-neutral DTO to standardise gateway responses. It decouples the core domain from specific API formats returned by different payment processors.
 * 
 * **Improvement Direction**: Maintain this clean separation. Ensure all new integration boundaries map their custom responses to this standard result structure.
 */
package com.aims.dto.payment;

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

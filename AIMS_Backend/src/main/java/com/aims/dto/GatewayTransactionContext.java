/**
 * SOLID Principles Analysis:
 * - **OCP/DIP (Open/Closed & Dependency Inversion) Violation**: Contains the PayPal-specific field `paypalOrderId`. Generic transaction DTOs must remain vendor-neutral. Adding support for another gateway (e.g. VNPay, Stripe) should not require modifying or polluting a core data object.
 * 
 * **Improvement Direction**: Rename `paypalOrderId` to a generic `gatewayOrderId` or `transactionToken` to ensure the DTO remains generic and reusable across various payment gateways.
 */
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

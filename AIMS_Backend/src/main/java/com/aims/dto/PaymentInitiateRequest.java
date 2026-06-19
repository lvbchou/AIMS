/**
 * SOLID Principles Analysis:
 * - **OCP (Open/Closed Principle) Limitation**: The DTO has no parameter indicating the selected payment gateway or method. If a user wishes to switch payment methods at runtime, the API design would break or require modification to accommodate extra selectors.
 * 
 * **Improvement Direction**: Add a `paymentMethod` or `gatewayType` field so that the request payload can support multiple polymorphic payment routes dynamically.
 */
package com.aims.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * PaymentInitiateRequest - input data transfer object to initiate a payment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequest {
    private long amount; // Total payment amount in VND (e.g., 150000)
    private String orderId;
}

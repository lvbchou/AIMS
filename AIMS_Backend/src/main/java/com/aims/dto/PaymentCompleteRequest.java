/**
 * SOLID Principles Analysis:
 * - **OCP (Open/Closed Principle) Limitation**: While the request structure is simple, it only contains a single `token` parameter. Other payment gateways might require multiple callback variables or complex verification hashes.
 * 
 * **Improvement Direction**: Add a metadata map or a generic key-value payload map to allow other gateways to supply arbitrary fields without altering the core request class.
 */
package com.aims.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * PaymentCompleteRequest - input data transfer object to capture/complete a payment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompleteRequest {
    private String token; // The PayPal Order ID / token used to capture the transaction
}

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

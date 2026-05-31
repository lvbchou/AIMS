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
}

package com.aims.gateway;

import java.math.BigDecimal;

/**
 * PaymentRefundParams - DTO containing gateway-neutral details required to process a refund.
 */
public record PaymentRefundParams(
    String transactionId,
    BigDecimal amount,
    String currency
) {}

package com.aims.dto;

import lombok.Builder;
import lombok.Value;

/**
 * GatewayRefundResult - encapsulates the outcome of a refund attempt from a payment gateway.
 */
@Value
@Builder
public class GatewayRefundResult {
    String refundId;
    String status;
    boolean success;
    String message;
}

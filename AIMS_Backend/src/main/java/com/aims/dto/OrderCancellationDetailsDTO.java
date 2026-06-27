package com.aims.dto;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.List;
import com.aims.dto.order.InvoiceLineItemDTO;

/**
 * OrderCancellationDetailsDTO - carries all structured details needed by the frontend cancellation screen
 * including order status, eligibility, line items, recipient details, and successful transaction info.
 */
@Value
@Builder
public class OrderCancellationDetailsDTO {
    String orderId;
    String orderStatus;
    boolean eligibleForCancellation;
    
    // Invoice details
    String invoiceId;
    LocalDateTime issueDate;
    List<InvoiceLineItemDTO> lineItems;
    long totalProductPriceExclVat;
    long totalProductPriceInclVat;
    long deliveryFee;
    long totalAmountToBePaid;
    
    // Shipping details
    String recipientName;
    String phoneNumber;
    String email;
    String detailAddress;
    String province;
    
    // Payment details
    String paymentMethod;
    String transactionId;
    String transactionContent;
    String transactionTimeDisplay;
}

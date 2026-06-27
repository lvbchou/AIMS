package com.aims.service;

import com.aims.dto.OrderCancellationDetailsDTO;
import com.aims.dto.common.ApiResponse;

/**
 * IOrderCancellationService - interface defining operations for the order cancellation use case.
 */
public interface IOrderCancellationService {

    /**
     * Retrieves the cancellation details for the given order, including eligibility checks.
     *
     * @param orderId the order ID.
     * @return the cancellation details DTO.
     */
    OrderCancellationDetailsDTO getCancellationDetails(String orderId);

    /**
     * Cancels the given order and processes automatic refund if applicable.
     *
     * @param orderId the order ID.
     * @return an ApiResponse detailing the cancellation status.
     */
    ApiResponse<String> cancelOrder(String orderId);
}

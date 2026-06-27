package com.aims.controller;

import com.aims.dto.OrderCancellationDetailsDTO;
import com.aims.dto.common.ApiResponse;
import com.aims.service.IOrderCancellationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OrderCancellationController - exposes APIs for Order Cancellation & PayPal Refund use case.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Cancellation", description = "APIs for canceling orders and processing automatic refunds")
public class OrderCancellationController {

    private final IOrderCancellationService orderCancellationService;

    @GetMapping("/{orderId}/cancel-details")
    @Operation(
            summary = "Get order cancellation details",
            description = "Retrieve order, invoice, shipping and payment details for displaying on the cancel confirmation screen."
    )
    public ResponseEntity<OrderCancellationDetailsDTO> getCancellationDetails(
            @PathVariable String orderId) {
        OrderCancellationDetailsDTO details = orderCancellationService.getCancellationDetails(orderId);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(
            summary = "Cancel order and process refund",
            description = "Cancel order, call payment gateway to process full refund, persist refund transaction, update status and notify customer."
    )
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @PathVariable String orderId) {
        ApiResponse<String> response = orderCancellationService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }
}

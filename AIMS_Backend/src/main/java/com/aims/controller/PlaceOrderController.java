/*
 * LAB12 SOLID REVIEW:
 * No major SRP violation is identified in this controller because it only
 * coordinates HTTP requests and delegates business work to PlaceOrderService.
 * Improvement direction: keep validation, pricing, persistence, and stock
 * update rules out of this class so it remains a thin delivery adapter.
 */
package com.aims.controller;

import com.aims.dto.order.InvoiceScreenDTO;
import com.aims.dto.order.CalculateShippingRequest;
import com.aims.dto.order.ConfirmPaidOrderRequest;
import com.aims.dto.order.CreateInvoiceRequest;
import com.aims.dto.order.PlaceOrderRequest;
import com.aims.dto.common.ApiResponse;
import com.aims.dto.order.InvoiceResponse;
import com.aims.service.PlaceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Place Order", description = "APIs for the Place Order use case")
public class PlaceOrderController {

    private final PlaceOrderService placeOrderService;

    @PostMapping("/place")
    @Operation(
            summary = "Validate cart and proceed to delivery",
            description = "Validate product availability for the submitted cart and store the cart in session for the next Place Order steps."
    )
    public ResponseEntity<ApiResponse<String>> placeOrder(
            @RequestBody(required = false) PlaceOrderRequest request) {
        placeOrderService.placeOrder(request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Order is valid. Please enter delivery information.",
                "PROCEED_TO_DELIVERY_INFO"
        ));
    }

    @PostMapping("/calculate-shipping")
    @Operation(
            summary = "Calculate shipping fee",
            description = "Calculate shipping fee from the delivery province and the cart currently stored in session."
    )
    public ResponseEntity<ApiResponse<Long>> calculateShipping(
            @RequestBody CalculateShippingRequest request) {
        Long shippingFee = placeOrderService.calculateShipping(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Shipping fee calculated successfully.", shippingFee));
    }

    @PostMapping("/create-invoice")
    @Operation(
            summary = "Create invoice",
            description = "Create order, delivery, order items, and invoice for the cart currently stored in session."
    )
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse invoice = placeOrderService.createInvoice(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invoice created successfully.", invoice));
    }

    @GetMapping("/{orderId}/invoice")
    @Operation(
            summary = "Get invoice screen",
            description = "Return order, delivery, line-item, and total data needed to display the invoice before payment."
    )
    public ResponseEntity<InvoiceScreenDTO> getInvoiceScreen(@PathVariable String orderId) {
        return ResponseEntity.ok(placeOrderService.getInvoiceScreen(orderId));
    }

    @PostMapping("/confirm-paid")
    @Operation(
            summary = "Confirm paid order",
            description = "Handle post-payment services after the customer clicks I have paid the order. Payment gateway verification is intentionally out of scope here."
    )
    public ResponseEntity<ApiResponse<InvoiceResponse>> confirmPaidOrder(
            @RequestBody ConfirmPaidOrderRequest request) {
        InvoiceResponse invoice = placeOrderService.confirmPaidOrder(request.getOrderId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Paid order confirmed successfully.", invoice));
    }

}

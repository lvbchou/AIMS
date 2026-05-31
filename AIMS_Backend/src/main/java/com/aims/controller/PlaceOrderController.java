package com.aims.controller;

import com.aims.dto.request.DeliveryInfoRequest;
import com.aims.dto.request.ConfirmPaidOrderRequest;
import com.aims.dto.request.PlaceOrderRequest;
import com.aims.dto.response.ApiResponse;
import com.aims.dto.response.InvoiceResponse;
import com.aims.service.PlaceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @RequestBody(required = false) PlaceOrderRequest request,
            HttpSession session) {
        placeOrderService.processOrder(request, session);
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
            @RequestBody DeliveryInfoRequest deliveryInfo,
            HttpSession session) {
        Long shippingFee = placeOrderService.calculateShippingFee(deliveryInfo, session);
        return ResponseEntity.ok(new ApiResponse<>(true, "Shipping fee calculated successfully.", shippingFee));
    }

    @PostMapping("/create-invoice")
    @Operation(
            summary = "Create invoice",
            description = "Create order, delivery, order items, and invoice for the cart currently stored in session."
    )
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @RequestBody DeliveryInfoRequest deliveryInfo,
            HttpSession session) {
        InvoiceResponse invoice = placeOrderService.createInvoice(deliveryInfo, session);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invoice created successfully.", invoice));
    }

    @PostMapping("/confirm-paid")
    @Operation(
            summary = "Confirm paid order",
            description = "Handle post-payment services after the customer clicks I have paid the order. Payment gateway verification is intentionally out of scope here."
    )
    public ResponseEntity<ApiResponse<InvoiceResponse>> confirmPaidOrder(
            @RequestBody ConfirmPaidOrderRequest request,
            HttpSession session) {
        InvoiceResponse invoice = placeOrderService.confirmPaidOrder(request.getOrderId(), session);
        return ResponseEntity.ok(new ApiResponse<>(true, "Paid order confirmed successfully.", invoice));
    }
}

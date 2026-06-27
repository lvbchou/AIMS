/**
 * SOLID Principles Analysis:
 * - **SRP (Single Responsibility Principle) Violation**: The controller performs domain instantiation and business logic (creating `Cart`, `Order`, `Invoice`, configuring shipping fees, updating repositories) instead of delegating to a dedicated service or domain factory.
 * - **DIP (Dependency Inversion Principle) Violation**: Tightly couples to the concrete service class `PayThroughPaymentGatewayService` instead of depending on an abstract service interface.
 * 
 * **Improvement Direction**:
 * 1. Move all entity creation, shipping calculations, and repository updates out of the controller into the service layer or a dedicated factory class.
 * 2. Depend on abstract interfaces rather than concrete service classes.
 */
package com.aims.controller;

import com.aims.dto.payment.GatewayTransactionContext;
import com.aims.dto.payment.PaymentCompleteResponse;
import com.aims.dto.payment.PaymentCompleteRequest;
import com.aims.dto.payment.PaymentInitiateRequest;
import com.aims.exception.PaymentException;
import com.aims.service.IPayThroughPaymentGatewayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * PayThroughPaymentGatewayController - handles incoming API requests from the frontend
 * to initiate and complete credit card payments via the integrated payment gateway (PayPal).
 */
@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment Gateway", description = "PayPal payment initiation and completion")
public class PayThroughPaymentGatewayController {

    private final IPayThroughPaymentGatewayService paymentService;

    /**
     * Constructs the controller with the payment service abstraction injected.
     *
     * <p><strong>DIP (P3.1):</strong> Depends on {@link IPayThroughPaymentGatewayService}
     * rather than on the concrete {@link com.aims.service.PayThroughPaymentGatewayService}.
     * This allows the controller to be tested in isolation with a mock service and
     * allows future service implementations to be swapped without modifying this class.</p>
     *
     * @param paymentService the payment service abstraction.
     */
    public PayThroughPaymentGatewayController(IPayThroughPaymentGatewayService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payment/initiate
     * Receives the existing checkout order ID and returns the gateway redirect URL.
     */
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, String>> initiatePayment(@RequestBody PaymentInitiateRequest request) {
        try {
            GatewayTransactionContext context = paymentService.createPaymentForOrder(request.getOrderId());

            Map<String, String> response = new HashMap<>();
            response.put("approvalUrl", context.getApprovalUrl());
            response.put("orderId", request.getOrderId());
            response.put("transactionId", context.getPaypalOrderId());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Gateway Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/payment/complete
     * Captures and completes the transaction using the PayPal token.
     */
    @PostMapping("/complete")
    public ResponseEntity<PaymentCompleteResponse> completePayment(@RequestBody PaymentCompleteRequest request) {
        try {
            if (request.getToken() == null || request.getToken().isBlank()) {
                return ResponseEntity.badRequest().body(PaymentCompleteResponse.builder()
                        .status("FAILED")
                        .message("Payment token is required.")
                        .build());
            }

            return ResponseEntity.ok(paymentService.completePayment(request.getToken()));
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(PaymentCompleteResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentCompleteResponse.builder()
                            .status("FAILED")
                            .message("Unexpected error: " + e.getMessage())
                            .build());
        }
    }
}

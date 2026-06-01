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

import com.aims.dto.PaymentCompleteRequest;
import com.aims.dto.PaymentInitiateRequest;
import com.aims.entity.Cart;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.exception.PaymentException;
import com.aims.repository.IOrderRepository;
import com.aims.service.PayThroughPaymentGatewayService;
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
public class PayThroughPaymentGatewayController {

    private final PayThroughPaymentGatewayService paymentService;
    private final IOrderRepository orderRepository;

    public PayThroughPaymentGatewayController(
            PayThroughPaymentGatewayService paymentService,
            IOrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
    }

    /**
     * POST /api/payment/initiate
     * Receives order amount from frontend, creates Invoice/Order, and returns the gateway redirect URL.
     */
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, String>> initiatePayment(@RequestBody PaymentInitiateRequest request) {
        try {
            // 1. Create a Cart containing a dummy item so it satisfies validation (not empty)
            Cart cart = new Cart();
            cart.getItems().add("AIMS Shopping Cart Product");

            // 2. Create the Order based on the Cart
            Order order = new Order(cart);
            
            // 3. Create the Invoice associated with the Order
            Invoice invoice = new Invoice(order);
            invoice.setSubTotalIncVAT(request.getAmount());
            invoice.setShippingFee(20000L); // Standard flat shipping fee: 20k VND
            invoice.calculateTotalAmount();

            // 4. Save the Order in the repository so completePayment can locate it later
            orderRepository.updateOrder(order);

            // 5. Call the payment gateway service to initiate a PayPal session
            String approvalUrl = paymentService.createPayment(invoice);

            // 6. Return approval URL to frontend
            Map<String, String> response = new HashMap<>();
            response.put("approvalUrl", approvalUrl);
            response.put("orderId", order.getOrderId());
            
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
    public ResponseEntity<Map<String, String>> completePayment(@RequestBody PaymentCompleteRequest request) {
        try {
            if (request.getToken() == null || request.getToken().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment token is required."));
            }

            // 1. Execute and capture the payment using the service
            paymentService.completePayment(request.getToken());

            // 2. Return success status
            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Payment captured and order updated to APPROVED successfully.");
            
            return ResponseEntity.ok(response);
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of("status", "FAILED", "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "FAILED", "error", "Unexpected error: " + e.getMessage()));
        }
    }
}

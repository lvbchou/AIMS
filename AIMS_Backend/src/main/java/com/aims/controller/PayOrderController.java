package com.aims.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aims.dto.InvoiceScreenDTO;
import com.aims.dto.OrderConfirmationDTO;
import com.aims.dto.VietQRCodeResponseDTO;
import com.aims.dto.VietQRCallbackRequestDTO;
import com.aims.service.PayOrderService;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 *
 * This controller exposes only UC003 payment endpoints and delegates each
 * request to the service layer with minimal data.
 *
 * SOLID VIOLATION: Single Responsibility Principle (SRP)
 *
 * Problem: The getPaymentStatus method (line 70-78) constructs a response Map
 *   directly inside the controller, mixing HTTP routing with response-body
 *   construction logic. This controller also exposes both production endpoints
 *   (invoice, QR, callback) and testing-only endpoints (test-callback) in one class.
 * Impact: Changes to the response format require modifying the controller rather
 *   than a dedicated response builder. Test endpoints mixed with production
 *   endpoints risk accidental exposure in production.
 * Improvement:
 *   - Move response Map construction into the service layer (PayOrderService
 *     already provides getOrderPaymentStatus which returns a Map — use it
 *     consistently)
 *   - Separate test-only endpoints into a dedicated TestPaymentController
 *     that can be conditionally loaded via Spring profiles
 *
 * SOLID: Open/Closed Principle (OCP) - Not Violated
 *
 * The controller delegates all business logic to the service layer. Adding a
 * new payment method does not require modifying existing endpoint handlers.
 *
 * SOLID: Liskov Substitution Principle (LSP) - Not Violated
 *
 * This class does not participate in an inheritance hierarchy.
 *
 * SOLID: Interface Segregation Principle (ISP) - Not Violated
 *
 * This class does not implement any interface. As a REST controller, it
 * exposes HTTP endpoints rather than programmatic interfaces.
 *
 * SOLID VIOLATION: Dependency Inversion Principle (DIP)
 *
 * Problem: This high-level controller depends directly on the concrete class
 *   PayOrderService rather than an abstraction (interface). The constructor
 *   injects the implementation directly.
 * Impact: Replacing the service implementation (e.g. for integration testing
 *   with a mock payment processor) requires modifying the injection or using
 *   framework-specific test utilities instead of clean interface substitution.
 * Improvement:
 *   - Extract an IPayOrderService interface from PayOrderService
 *   - Inject the interface in the controller constructor
 *   - This allows easy substitution of mock implementations for testing
 *
 * @author Team 03
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class PayOrderController {

    private final PayOrderService payOrderService;

    public PayOrderController(PayOrderService payOrderService) {
        this.payOrderService = payOrderService;
    }

    /**
     * Returns the invoice view before payment.
     *
     * @param orderId the order identifier.
     * @return invoice data.
     */
    @GetMapping("/orders/{orderId}/pay/invoice")
    public ResponseEntity<InvoiceScreenDTO> getInvoiceForPayment(@PathVariable String orderId) {
        InvoiceScreenDTO dto = payOrderService.getInvoiceScreen(orderId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Creates a VietQR payload for the order.
     *
     * @param orderId the order identifier.
     * @return payment QR data.
     */
    @PostMapping("/orders/{orderId}/pay/vietqr/qrcode")
    public ResponseEntity<?> requestVietQrQrCode(@PathVariable String orderId) {
        VietQRCodeResponseDTO dto = payOrderService.requestVietQrDisplay(orderId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Returns the payment status for a transaction ID.
     *
     * @param transactionId transaction identifier to check.
     * @return a JSON map containing payment status information.
     */
    @GetMapping("/payment/status/{transactionId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String transactionId) {
        boolean success = payOrderService.isPaymentSuccessful(transactionId);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "transactionId", transactionId,
                "status", success ? "COMPLETED" : "PENDING"
        ));
    }

    /**
     * Triggers a test callback to the real VietQR Sandbox API to simulate a successful payment.
     * VietQR will then call back our transaction-sync endpoint.
     *
     * @param orderId order identifier.
     * @return result of the test callback.
     */
    @PostMapping("/orders/{orderId}/pay/vietqr/test-callback")
    public ResponseEntity<?> triggerVietQRTestCallback(@PathVariable String orderId) {
        java.util.Map<String, Object> result = payOrderService.triggerVietQRTestCallback(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns the payment status for an order by looking up its latest transaction.
     *
     * @param orderId order identifier.
     * @return a JSON map with success, transactionId, and status.
     */
    @GetMapping("/orders/{orderId}/pay/status")
    public ResponseEntity<?> getOrderPaymentStatus(@PathVariable String orderId) {
        java.util.Map<String, Object> result = payOrderService.getOrderPaymentStatus(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * Receives a VietQR callback that has been mapped to a DTO.
     *
     * @param body payment callback data.
     * @return an empty response after processing.
     */
    @PostMapping("/payments/vietqr/callback")
    public ResponseEntity<Void> vietQrPaymentCallback(@RequestBody VietQRCallbackRequestDTO body) {
        payOrderService.handleVietQrPaymentCallback(body);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the post-payment confirmation data for an order.
     *
     * @param orderId the order identifier.
     * @return payment confirmation data.
     */
    @GetMapping("/orders/{orderId}/pay/confirmation")
    public ResponseEntity<OrderConfirmationDTO> getPayOrderConfirmation(@PathVariable String orderId) {
        OrderConfirmationDTO dto = payOrderService.getOrderConfirmation(orderId);
        return ResponseEntity.ok(dto);
    }
}
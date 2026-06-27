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

import com.aims.dto.order.OrderConfirmationDTO;
import com.aims.dto.payment.VietQRCodeResponseDTO;
import com.aims.dto.payment.VietQRCallbackRequestDTO;
import com.aims.service.payment.vietqr.InvoiceQueryService;
import com.aims.service.payment.vietqr.PaymentStatusService;
import com.aims.service.payment.vietqr.VietQrPaymentService;
import com.aims.subsystem.IPaymentQRCode;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 *
 * This controller exposes only UC003 payment endpoints and delegates each
 * request to the appropriate focused service — no business logic lives here.
 *
 * Refactored from the original PayOrderService monolith into three focused services:
 *   - InvoiceQueryService  : invoice display + post-payment confirmation
 *   - VietQrPaymentService : QR generation + callback processing
 *   - PaymentStatusService : read-only payment status polling
 *
 * @author Team 03
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Pay Order", description = "VietQR payment, status checks, and callbacks")
@RequiredArgsConstructor
public class PayOrderController {

    private final InvoiceQueryService invoiceQueryService;
    private final VietQrPaymentService paymentService;
    private final PaymentStatusService paymentStatusService;
    private final IPaymentQRCode paymentQRCode;

    /**
     * Creates a VietQR payload for the order.
     *
     * @param orderId the order identifier.
     * @return payment QR data.
     */
    @PostMapping("/orders/{orderId}/pay/vietqr/qrcode")
    public ResponseEntity<?> requestVietQrQrCode(@PathVariable String orderId) {
        VietQRCodeResponseDTO dto = paymentService.requestVietQrDisplay(orderId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Returns the payment status for a specific transaction ID.
     *
     * @param transactionId transaction identifier to check.
     * @return a JSON map containing success flag, transactionId, and status string.
     */
    @GetMapping("/payment/status/{transactionId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String transactionId) {
        boolean success = paymentStatusService.isPaymentSuccessful(transactionId);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "transactionId", transactionId,
                "status", success ? "COMPLETED" : "PENDING"
        ));
    }

    /**
     * Triggers a mock payment callback to simulate a successful VietQR payment.
     * Used in development to avoid E222 sandbox errors.
     *
     * @param orderId order identifier.
     * @return result of the mock callback.
     */
    @PostMapping("/orders/{orderId}/pay/vietqr/test-callback")
    public ResponseEntity<?> triggerVietQRTestCallback(@PathVariable String orderId) {
        Map<String, Object> result = paymentQRCode.triggerTestCallback(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns the payment status of an order by looking up its latest transaction.
     * Used by the frontend for polling after displaying the QR code.
     *
     * @param orderId order identifier.
     * @return a JSON map with success, transactionId, and status.
     */
    @GetMapping("/orders/{orderId}/pay/status")
    public ResponseEntity<?> getOrderPaymentStatus(@PathVariable String orderId) {
        Map<String, Object> result = paymentStatusService.getOrderPaymentStatus(orderId);
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
        paymentService.handleVietQrPaymentCallback(body);
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
        OrderConfirmationDTO dto = invoiceQueryService.getOrderConfirmation(orderId);
        return ResponseEntity.ok(dto);
    }
}

package com.aims.subsystem.vietqr.callback;

import com.aims.exception.InvoiceNotFoundException;
import com.aims.exception.OrderNotFoundException;
import com.aims.subsystem.vietqr.security.ICallbackAuthGuard;
import com.aims.subsystem.vietqr.security.IApiCredentialProvider;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aims.dto.vietqr.TestCallbackRequest;
import com.aims.dto.vietqr.TestCallbackResponse;
import com.aims.dto.vietqr.TokenResponse;
import com.aims.dto.vietqr.TransactionSyncRequest;
import com.aims.dto.vietqr.TransactionSyncResponse;
import com.aims.service.payment.vietqr.VietQrPaymentService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller mapping and routing webhook callback requests from the VietQR payment gateway.
 */
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VietQRCallbackEndpoint {

    // ── Injected via constructor (DIP: all interfaces or focused concrete services) ──
    private final VietQrPaymentService paymentService;
    private final ICallbackAuthGuard authValidator;      // DIP: interface, NOT VietQRController
    private final OrderIdResolver orderIdResolver;       // SRP: orderId resolution extracted

    // ─────────────────────────────────────────────────────────────
    // Endpoint 1 — Raw webhook (no auth required from VietQR side)
    // ─────────────────────────────────────────────────────────────

    /**
     * Receives the raw JSON webhook payload posted directly by VietQR.
     *
     * VietQR sends this automatically when a payment event occurs.
     * No Bearer token is required — authentication is via payload signature
     * (handled inside {@link VietQrPaymentService#handleVietQrWebhook}).
     *
     * @param callbackData raw JSON string sent by VietQR.
     */
    @PostMapping("/callback")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveCallback(@RequestBody String callbackData) {
        paymentService.handleVietQrWebhook(callbackData);
    }

    // ─────────────────────────────────────────────────────────────
    // Endpoint 2 — Token generation (VietQR needs a token first)
    // ─────────────────────────────────────────────────────────────

    /**
     * Issues a Bearer access token to VietQR after validating Basic credentials.
     *
     * VietQR calls this before sending transaction-sync callbacks so that
     * subsequent requests can carry a Bearer token instead of Basic credentials.
     *
     * @param authorizationHeader the {@code Authorization} header (Basic scheme).
     * @return 200 with a {@link TokenResponse}, or 401 if credentials are invalid.
     */
    @PostMapping("/vqr/api/token_generate")
    public ResponseEntity<TokenResponse> generateToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        System.out.println("[VietQR] Received generateToken request. Auth Header: " + authorizationHeader);

        // 1. Reject non-Basic headers immediately
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            System.out.println("[VietQR] Rejecting non-Basic or missing header.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Delegate credential validation to the injected abstraction (DIP)
        if (!authValidator.validate(authorizationHeader)) {
            System.out.println("[VietQR] Basic credential validation failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3. Return a short-lived Bearer token
        String token = authValidator.getTokenSecret();
        System.out.println("[VietQR] Token issued successfully: " + token);
        return ResponseEntity.ok(new TokenResponse(token, "Bearer", 300));
    }

    // ─────────────────────────────────────────────────────────────
    // Endpoint 3 — Synchronous callback (VietQR transaction-sync)
    // ─────────────────────────────────────────────────────────────

    /**
     * Receives a synchronous VietQR callback containing full transaction data.
     *
     * VietQR expects a structured JSON response confirming that we processed
     * the transaction. This endpoint validates the Bearer token, validates
     * required fields, resolves the orderId, then completes the payment.
     *
     * @param request             full transaction payload from VietQR.
     * @param authorizationHeader Bearer token previously issued via {@link #generateToken}.
     * @return 200 with a {@link TransactionSyncResponse} on success, or 401/400 on failure.
     */
    @PostMapping("/vqr/bank/api/transaction-sync")
    public ResponseEntity<TransactionSyncResponse> transactionSync(
            @RequestBody TransactionSyncRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        System.out.println("[VietQR] Received transaction-sync: " + request);

        // 1. Auth guard — reject if Bearer token is invalid
        if (!authValidator.validate(authorizationHeader)) {
            System.out.println("[VietQR] Auth failed. Header: " + authorizationHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TransactionSyncResponse.error("INVALID_TOKEN", "Invalid or expired token"));
        }

        // 2. Process and return result
        return processSync(
                request.bankAccount(),
                request.content(),
                request.amount(),
                request.transType(),
                request.orderId(),
                request.transactionTime(),
                request.referenceNumber(),
                request.bankCode());
    }



    // ─────────────────────────────────────────────────────────────
    // Private shared processing (SRP: extracted from endpoints)
    // ─────────────────────────────────────────────────────────────

    /**
     * Validates the callback payload, resolves the orderId, then delegates
     * payment completion to {@link VietQrPaymentService}.
     *
     * SRP: this method is a coordination step only — no parsing or DB logic here.
     *
     * @param bankAccount     receiving bank account.
     * @param content         transfer content string (used to infer orderId).
     * @param amount          payment amount.
     * @param transType       transaction type from VietQR.
     * @param orderId         orderId sent directly by VietQR (may be null).
     * @param transactionTime epoch-millis timestamp (may be null).
     * @param referenceNumber VietQR reference number (may be null).
     * @param bankCode        bank code from VietQR.
     * @return a structured response for VietQR to acknowledge.
     */
    private ResponseEntity<TransactionSyncResponse> processSync(
            String bankAccount,
            String content,
            Long amount,
            String transType,
            String orderId,
            Long transactionTime,
            String referenceNumber,
            String bankCode) {
        try {
            // Validate required fields
            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(TransactionSyncResponse.error("INVALID_PAYLOAD", "content is required"));
            }
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                        .body(TransactionSyncResponse.error("INVALID_PAYLOAD", "amount is required"));
            }
            if (transType == null || transType.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(TransactionSyncResponse.error("INVALID_PAYLOAD", "transType is required"));
            }
            if (bankAccount == null || bankAccount.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(TransactionSyncResponse.error("INVALID_PAYLOAD", "bankAccount is required"));
            }

            // Resolve orderId via chain-of-responsibility (OCP: parser chain is extensible)
            String resolvedOrderId = orderIdResolver.resolve(orderId, content);
            if (resolvedOrderId == null || resolvedOrderId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(TransactionSyncResponse.error("INVALID_PAYLOAD",
                                "unable to resolve orderId from callback content"));
            }

            // Delegate payment completion to the service layer
            String transactionId = paymentService.completeVietQrPayment(
                    resolvedOrderId, transactionTime, referenceNumber, amount);

            return ResponseEntity.ok(TransactionSyncResponse.success(transactionId));

        } catch (OrderNotFoundException | InvoiceNotFoundException ex) {
            // Order or invoice no longer exists (e.g. stale retry from a previous DB session).
            // Return 200 so VietQR stops retrying — there is nothing left to process.
            System.out.println("[VietQR] Webhook for unknown order ignored (stale retry): " + ex.getMessage());
            return ResponseEntity.ok(
                    TransactionSyncResponse.error("ORDER_NOT_FOUND", "Order not found, webhook acknowledged"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(TransactionSyncResponse.error("TRANSACTION_FAILED", ex.getMessage()));
        }
    }
}

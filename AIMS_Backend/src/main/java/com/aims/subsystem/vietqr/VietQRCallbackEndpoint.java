package com.aims.subsystem.vietqr;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aims.service.PayOrderService;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coupling level: Stamp Coupling.
 * Cohesion level: Logical Cohesion.
 *
 * This endpoint groups related callback routes and delegates the actual payment
 * handling to the service layer after routing the incoming payload.
 *
 * SOLID VIOLATION: Single Responsibility Principle (SRP)
 *
 * Problem: This class combines four distinct responsibilities:
 *   1. HTTP endpoint routing for three different callback paths
 *      (receiveCallback, transactionSync, testTransactionCallback)
 *   2. Request validation and orderId resolution logic (processSync, resolveOrderId,
 *      restoreOrderId) — this is business logic embedded in a controller
 *   3. Authentication delegation (calling vietQRController.validateBearerToken)
 *   4. DTO definitions (TransactionSyncRequest, TransactionSyncResponse,
 *      TestCallbackRequest, TestCallbackResponse) — data classes defined as
 *      inner records inside the controller
 * Impact: Changes to the orderId resolution algorithm affect the endpoint class.
 *   DTO changes (e.g. adding a field) require modifying the controller file.
 *   The class has over 340 lines, well beyond the expected size for a controller.
 * Improvement:
 *   - Move resolveOrderId and restoreOrderId into a dedicated OrderIdResolver
 *     utility or into the service layer
 *   - Move inner record DTOs into the dto package as standalone classes
 *   - Split receiveCallback (raw webhook) into a separate controller from
 *     the synchronous callback endpoints
 *
 * SOLID VIOLATION: Open/Closed Principle (OCP)
 *
 * Problem: The resolveOrderId method uses a chain of if-else branches to parse
 *   different content formats ("Order #ORD-001", "Order ORD001", "VQR... ORD001",
 *   plain orderId). Adding support for a new format requires modifying this method.
 * Impact: New VietQR content formats or new payment providers with different
 *   content patterns force modification of this stable method.
 * Improvement:
 *   - Define an OrderIdParser interface with method String parse(String content)
 *   - Implement format-specific parsers (LegacyOrderIdParser, VQRPrefixParser, etc.)
 *   - Use a chain-of-responsibility pattern to try each parser in sequence
 *
 * SOLID: Liskov Substitution Principle (LSP) - Not Violated
 *
 * This class does not participate in an inheritance hierarchy.
 *
 * SOLID VIOLATION: Interface Segregation Principle (ISP)
 *
 * Problem: The inner record DTOs (TransactionSyncRequest, TestCallbackRequest,
 *   TransactionSyncResponse, TestCallbackResponse) are defined as nested types
 *   inside this controller. Any class that needs to reference these DTOs is
 *   forced to depend on the entire VietQRCallbackEndpoint class, even if it
 *   only needs one DTO type.
 * Impact: External modules or tests that reference these DTOs unnecessarily
 *   import the full controller class with all its dependencies.
 * Improvement:
 *   - Move each DTO into the dto package as a standalone top-level class
 *   - This decouples DTO consumers from the controller implementation
 *
 * SOLID VIOLATION: Dependency Inversion Principle (DIP)
 *
 * Problem: This controller depends on the concrete class VietQRController for
 *   authentication validation. It also depends on the concrete PayOrderService
 *   rather than an abstraction.
 * Impact: Swapping the authentication provider or payment service implementation
 *   requires modifying the constructor injection.
 * Improvement:
 *   - Inject an IAuthValidator interface instead of VietQRController for token validation
 *   - Inject an IPayOrderService interface instead of the concrete PayOrderService
 *
 * @author Team 03
 * @since 1.0.0
 */
@RestController
@org.springframework.web.bind.annotation.CrossOrigin(origins = "*")
public class VietQRCallbackEndpoint {

    private final PayOrderService payOrderService;
    private final VietQRController vietQRController;

    public VietQRCallbackEndpoint(PayOrderService payOrderService, VietQRController vietQRController) {
        this.payOrderService = payOrderService;
        this.vietQRController = vietQRController;
    }

    /**
     * Receives the raw webhook payload from VietQR.
     *
     * @param callbackData raw JSON payload sent by VietQR.
     */
    @PostMapping("/callback")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveCallback(@RequestBody String callbackData) {
        payOrderService.handleVietQrWebhook(callbackData);
    }

    public record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") int expiresIn) {}

    /**
     * Exposes the token generation endpoint for VietQR to obtain a Bearer token.
     */
    @PostMapping("/vqr/api/token_generate")
    public ResponseEntity<TokenResponse> generateToken(
            @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!vietQRController.validateBearerToken(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = vietQRController.getPartnerTokenSecret();
        return ResponseEntity.ok(new TokenResponse(token, "Bearer", 300));
    }

    /**
     * Receives a synchronous callback from VietQR and returns the processing
     * status.
     *
     * @param request             transaction data sent by VietQR.
     * @param authorizationHeader callback authorization header.
     * @return synchronous response for the transaction-sync flow.
     */
    @PostMapping("/vqr/bank/api/transaction-sync")
    public ResponseEntity<TransactionSyncResponse> transactionSync(
            @RequestBody TransactionSyncRequest request,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        System.out.println("Received transaction-sync callback from VietQR: " + request);

        if (!vietQRController.validateBearerToken(authorizationHeader)) {
            System.out.println("Callback validation failed! Header: " + authorizationHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TransactionSyncResponse.error("INVALID_TOKEN", "Invalid or expired token"));
        }

        return processSync(request.bankAccount(), request.content(), request.amount(), request.transType(),
                request.orderId(), request.transactionTime(), request.referenceNumber(), request.bankCode());
    }

    /**
     * Receives a test callback for verifying the processing flow without a real
     * webhook.
     *
     * @param request             test data sent by the client.
     * @param authorizationHeader authorization header for the test callback.
     * @return the processing status response for the test callback.
     */
    @PostMapping("/vqr/bank/api/test/transaction-callback")
    public ResponseEntity<TestCallbackResponse> testTransactionCallback(
            @RequestBody TestCallbackRequest request,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (!vietQRController.validateBearerToken(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TestCallbackResponse.failed("Invalid or expired token"));
        }

        ResponseEntity<TransactionSyncResponse> processed = processSync(
                request.bankAccount(),
                request.content(),
                request.amount(),
                request.transType(),
                null,
                null,
                null,
                request.bankCode());

        TransactionSyncResponse body = processed.getBody();
        if (body == null || body.error()) {
            String message = body == null ? "Callback processing failed" : body.toastMessage();
            return ResponseEntity.status(processed.getStatusCode()).body(TestCallbackResponse.failed(message));
        }

        return ResponseEntity.ok(TestCallbackResponse.success("Callback processed successfully"));
    }

    /**
     * Validates callback data and forwards it to the payment service.
     *
     * @param bankAccount     receiving bank account number.
     * @param content         transfer content.
     * @param amount          transaction amount.
     * @param transType       transaction type.
     * @param orderId         order identifier if VietQR sends it directly.
     * @param transactionTime transaction time as epoch millis.
     * @param referenceNumber transaction reference number.
     * @param bankCode        bank code.
     * @return synchronous response containing the processing result.
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

            String resolvedOrderId = resolveOrderId(orderId, content);
            if (resolvedOrderId == null || resolvedOrderId.isBlank()) {
                return ResponseEntity.badRequest().body(TransactionSyncResponse.error("INVALID_PAYLOAD",
                        "unable to resolve orderId from callback content"));
            }

            String transactionId = payOrderService.completeVietQrPayment(resolvedOrderId, transactionTime,
                    referenceNumber, amount);
            return ResponseEntity.ok(TransactionSyncResponse.success(transactionId));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(TransactionSyncResponse.error("TRANSACTION_FAILED", ex.getMessage()));
        }
    }

    /**
     * Resolves an order identifier from the callback payload.
     *
     * @param orderId order identifier sent directly in the callback.
     * @param content transfer content used to infer the order when needed.
     * @return the normalized order identifier, or {@code null} if it cannot be
     *         resolved.
     */
    private String resolveOrderId(String orderId, String content) {
        // Prefer the direct orderId if VietQR sends one.
        if (orderId != null && !orderId.isBlank()) {
            return restoreOrderId(orderId.trim());
        }
        if (content == null || content.isBlank()) {
            return null;
        }
        String trimmed = content.trim();
        // Backward compatibility for the old "Order #ORD-001" format.
        String marker = "Order #";
        int markerIdx = trimmed.indexOf(marker);
        if (markerIdx >= 0) {
            return trimmed.substring(markerIdx + marker.length()).trim();
        }
        // Handle "Order ORD001" format (without #) generated by VietQRController.
        String markerNoHash = "Order ";
        if (trimmed.startsWith(markerNoHash)) {
            String rawId = trimmed.substring(markerNoHash.length()).trim();
            return restoreOrderId(rawId);
        }
        // VietQR may prepend a terminal code; use the last token as the orderId.
        if (trimmed.startsWith("VQR") && trimmed.contains(" ")) {
            return restoreOrderId(trimmed.substring(trimmed.lastIndexOf(' ') + 1).trim());
        }
        // New format: content itself is the orderId.
        return restoreOrderId(trimmed);
    }

    /**
     * Restores the {@code ORD-001} format from a compact value such as
     * {@code ORD001}.
     *
     * @param raw raw orderId value.
     * @return the orderId with a dash inserted, or the original value if it does
     *         not match the pattern.
     */
    private static String restoreOrderId(String raw) {
        if (raw == null)
            return null;
        // Pattern: prefix letters followed by alphanumeric suffix (no dash present)
        // e.g. "ORD001" → "ORD-001", "ORD1K" → "ORD-1K"
        // Only apply if there is no dash already (i.e. it was compacted)
        String trimmed = raw.trim();
        if (trimmed.contains("-")) {
            return trimmed;
        }
        // Find where the first digit starts to split prefix from suffix
        int splitIdx = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isDigit(trimmed.charAt(i))) {
                splitIdx = i;
                break;
            }
        }
        if (splitIdx > 0) {
            String prefix = trimmed.substring(0, splitIdx);
            String suffix = trimmed.substring(splitIdx);
            // Only restore if prefix is all letters and suffix is non-empty
            if (prefix.chars().allMatch(Character::isLetter) && !suffix.isEmpty()) {
                return prefix + "-" + suffix;
            }
        }
        return trimmed;
    }

    /**
     * Request DTO for synchronous VietQR callbacks.
     */
    public record TransactionSyncRequest(
            @JsonProperty("bankaccount") String bankAccount,
            Long amount,
            String transType,
            String content,
            @JsonProperty("transactionid") String transactionId,
            @JsonProperty("transactiontime") Long transactionTime,
            @JsonProperty("referencenumber") String referenceNumber,
            @JsonProperty("orderId") String orderId,
            String terminalCode,
            String subTerminalCode,
            String serviceCode,
            String urlLink,
            String sign,
            @JsonProperty("bankCode") String bankCode) {
    }

    /**
     * Request DTO for test callbacks.
     */
    public record TestCallbackRequest(
            @JsonProperty("bankAccount") String bankAccount,
            String content,
            long amount,
            String transType,
            String bankCode) {
    }

    /**
     * Response DTO for synchronous callbacks.
     */
    public record TransactionSyncResponse(boolean error, String errorReason, String toastMessage,
            Map<String, String> object) {

        /**
         * Creates a successful transaction-sync response.
         *
         * @param refTransactionId reference transaction ID.
         * @return a success response.
         */
        public static TransactionSyncResponse success(String refTransactionId) {
            return new TransactionSyncResponse(false, null, "Transaction processed successfully",
                    Map.of("reftransactionid", refTransactionId));
        }

        /**
         * Creates an error response for transaction-sync.
         *
         * @param errorReason internal error code.
         * @param message     message returned to the client.
         * @return an error response.
         */
        public static TransactionSyncResponse error(String errorReason, String message) {
            return new TransactionSyncResponse(true, errorReason, message, null);
        }
    }

    /**
     * Response DTO for test callbacks.
     */
    public record TestCallbackResponse(String status, String message) {

        /**
         * Creates a successful test response.
         *
         * @param message result message.
         * @return a success response.
         */
        public static TestCallbackResponse success(String message) {
            return new TestCallbackResponse("SUCCESS", message);
        }

        /**
         * Creates a failed test response.
         *
         * @param message error message.
         * @return a failure response.
         */
        public static TestCallbackResponse failed(String message) {
            return new TestCallbackResponse("FAILED", message);
        }
    }
}

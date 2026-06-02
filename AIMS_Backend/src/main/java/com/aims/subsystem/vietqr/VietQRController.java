package com.aims.subsystem.vietqr;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aims.entity.Delivery;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.entity.PaymentResult;
import com.aims.entity.QRCode;
import com.aims.repository.DeliveryRepository;
import com.aims.repository.InvoiceRepository;
import com.aims.subsystem.IPaymentQRCode;
import com.aims.exception.CallbackVerificationException;

/**
 * Coupling level: Stamp Coupling.
 * Cohesion level: Functional Cohesion.
 *
 * This facade groups QR generation and callback validation under one payment
 * responsibility, but it still receives the full order object to build QR data.
 *
 * SOLID VIOLATION: Single Responsibility Principle (SRP)
 *
 * Problem: This class handles three distinct responsibilities:
 *   1. QR code generation orchestration (getQRCode) — building QR data from
 *      order, invoice, and delivery entities
 *   2. Callback payload verification (checkPaymentStatus) — parsing and
 *      validating VietQR webhook payloads
 *   3. Authentication token management (validateBearerToken, getPartnerTokenSecret)
 *      — validating Basic/Bearer authorization headers
 * Impact: Modifying the authentication mechanism (e.g. switching from Basic to
 *   OAuth2) requires changing the same class that generates QR codes. Testing
 *   QR generation in isolation requires mocking authentication-related fields.
 * Improvement:
 *   - Extract a VietQRAuthValidator class for validateBearerToken and
 *     getPartnerTokenSecret
 *   - Keep VietQRController focused solely on QR generation and callback parsing
 *     as defined by the IPaymentQRCode interface contract
 *
 * SOLID VIOLATION: Open/Closed Principle (OCP)
 *
 * Problem: The validateBearerToken method uses if-else branching to handle
 *   different authentication schemes (Basic and Bearer). Adding a new auth
 *   scheme (e.g. API-Key, OAuth2) requires modifying this method directly.
 * Impact: Each new authentication mechanism requires editing a stable, tested
 *   method, increasing regression risk.
 * Improvement:
 *   - Define an AuthSchemeValidator interface with method boolean validate(String header)
 *   - Implement BasicAuthValidator and BearerTokenValidator separately
 *   - Use a chain-of-responsibility or registry pattern to evaluate validators
 *
 * SOLID: Liskov Substitution Principle (LSP) - Not Violated
 *
 * VietQRController implements IPaymentQRCode faithfully. Both getQRCode and
 * checkPaymentStatus honor the interface contract without throwing unexpected
 * exceptions or weakening postconditions.
 *
 * SOLID: Interface Segregation Principle (ISP) - Not Violated
 *
 * IPaymentQRCode defines only two methods (getQRCode and checkPaymentStatus),
 * both of which are relevant to and implemented by this class. The interface
 * is appropriately narrow.
 *
 * SOLID VIOLATION: Dependency Inversion Principle (DIP)
 *
 * Problem: This class directly depends on concrete repository implementations
 *   (InvoiceRepository, DeliveryRepository) and the concrete VietQRBoundary
 *   class. A high-level subsystem controller should depend on abstractions
 *   rather than concrete data-access and HTTP-transport classes.
 * Impact: Replacing the data source (e.g. switching from JPA to a remote API)
 *   or the HTTP client library requires modifying this class. Unit testing
 *   requires mocking concrete classes rather than interfaces.
 * Improvement:
 *   - Inject an IPaymentDataProvider interface instead of raw repositories
 *   - Define an IVietQRBoundary interface for the HTTP boundary layer
 *   - VietQRController should only depend on these abstractions
 *
 * @author Team 03
 * @since 1.0.0
 */
@Service
public class VietQRController implements IPaymentQRCode {

    private static final Logger log = LoggerFactory.getLogger(VietQRController.class);

    private final VietQRBoundary boundary;
    private final InvoiceRepository invoiceRepository;
    private final DeliveryRepository deliveryRepository;
    private final String merchantId;
    private final String apiKey;
    private final String partnerUsername;
    private final String partnerPassword;
    private final String partnerTokenSecret;

    public VietQRController(
            VietQRBoundary boundary,
            InvoiceRepository invoiceRepository,
            DeliveryRepository deliveryRepository,
            @Value("${vietqr.merchant-id:}") String merchantId,
            @Value("${vietqr.api-key:}") String apiKey,
            @Value("${vietqr.partner-username:}") String partnerUsername,
            @Value("${vietqr.partner-password:}") String partnerPassword,
            @Value("${vietqr.partner-token-secret:}") String partnerTokenSecret) {
        this.boundary = boundary;
        this.invoiceRepository = invoiceRepository;
        this.deliveryRepository = deliveryRepository;
        this.merchantId = merchantId;
        this.apiKey = apiKey;
        this.partnerUsername = partnerUsername;
        this.partnerPassword = partnerPassword;
        this.partnerTokenSecret = partnerTokenSecret;
    }

    /**
     * Generates a payment QR from order data.
     *
     * @param order source order; it must exist and have matching invoice and
     *              delivery records.
     * @return the VietQR payload built from the order.
     * @throws IllegalArgumentException if {@code order} or related data is missing.
     */
    @Override
    public QRCode getQRCode(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order is required");
        }

        String orderId = java.util.Objects.requireNonNull(order.getOrderId(), "orderId is required");
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("invoice is required for order " + orderId));
        Delivery delivery = deliveryRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("delivery is required for order " + orderId));

        long amountToPay = invoice.getSubTotalIncVAT() + invoice.getShippingFee();

        // String OrderId to 13
        String safeOrderId = orderId.replace("-", "");
        if (safeOrderId.length() > 13) {
            safeOrderId = safeOrderId.substring(safeOrderId.length() - 13);
        }
        String content = "Order " + safeOrderId;

        // If token retrieval fails, still allow a static QR fallback so the payment
        // flow continues.
        QRAccessTokenRequest tokenRequest = new QRAccessTokenRequest(merchantId, apiKey);
        QRAccessToken accessToken = new QRAccessToken();
        try {
            accessToken = boundary.requestAccessToken(tokenRequest);
        } catch (RuntimeException ex) {
            log.warn("VietQR token request failed, using static QR fallback: {}", ex.getMessage());
        }

        // Build the QR request from the current order and recipient details.
        QRGenerateRequest generateRequest = boundary.createGenerateRequest(
                content,
                amountToPay,
                orderId,
                delivery.getRecipientName());
        generateRequest.setAccessToken(accessToken.getAccessToken());

        // Call the boundary for the QR payload; it will fallback if the VietQR API
        // fails.
        String response = boundary.getQRCode(generateRequest);

        // Parse the returned payload into a domain object for direct use by callers.
        QRCode qrCode = new QRCode();
        qrCode.parseQRCodeResponse(response);
        if (qrCode.getQrCode() == null || qrCode.getQrCode().isBlank()) {
            qrCode.setQrCode(response);
        }
        if (qrCode.getQrLink() == null || qrCode.getQrLink().isBlank()) {
            qrCode.setQrLink(firstNonBlank(qrCode.getContent(), orderId));
        }
        qrCode.setOrderId(orderId);
        qrCode.setContent(content);
        return qrCode;
    }

    /**
     * Validates and parses a payment callback payload.
     *
     * @param callbackData raw callback payload received from VietQR.
     * @return the parsed payment result.
     * @throws IllegalArgumentException      if the payload is blank.
     * @throws CallbackVerificationException if the checksum is invalid.
     */
    @Override
    public PaymentResult checkPaymentStatus(String callbackData) {
        if (callbackData == null || callbackData.isBlank()) {
            throw new IllegalArgumentException("callback payload is required");
        }

        VietQRCallbackHandler handler = new VietQRCallbackHandler(callbackData);
        if (!handler.verifyChecksum(apiKey)) {
            throw new CallbackVerificationException("Invalid VietQR callback checksum");
        }

        return handler.toPaymentResult();
    }

    /**
     * Returns the first non-blank value from the provided list.
     *
     * @param values candidate values in priority order.
     * @return the first non-blank value, or {@code null} if none exist.
     */
    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Validates the Authorization header sent by VietQR callbacks.
     *
     * Supports either {@code Basic} authentication using the configured
     * username/password pair or a {@code Bearer} token matching the configured
     * secret.
     *
     * @param authorizationHeader the Authorization header value.
     * @return {@code true} if the header is valid; otherwise {@code false}.
     */
    public boolean validateBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return false;
        }
        if (authorizationHeader.startsWith("Basic ")) {
            try {
                String b64 = authorizationHeader.substring("Basic ".length()).trim();
                byte[] decoded = Base64.getDecoder().decode(b64);
                String creds = new String(decoded, StandardCharsets.UTF_8);
                String expected = (partnerUsername == null ? "" : partnerUsername) + ":"
                        + (partnerPassword == null ? "" : partnerPassword);
                return creds.equals(expected);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        if (authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring("Bearer ".length()).trim();
            return token.equals(partnerTokenSecret == null ? "" : partnerTokenSecret);
        }
        return false;
    }

    /**
     * Exposes the partner token secret for use by the callback endpoint.
     *
     * @return the configured partner token secret.
     */
    public String getPartnerTokenSecret() {
        return partnerTokenSecret == null ? "" : partnerTokenSecret;
    }

}

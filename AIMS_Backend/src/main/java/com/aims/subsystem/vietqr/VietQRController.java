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
 * <p>
 * This facade groups QR generation and callback validation under one payment
 * responsibility, but it still receives the full order object to build QR data.
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
     * <p>
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

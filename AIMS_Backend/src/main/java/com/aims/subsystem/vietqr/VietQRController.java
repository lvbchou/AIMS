package com.aims.subsystem.vietqr;

import com.aims.subsystem.vietqr.dto.QRAccessToken;
import com.aims.subsystem.vietqr.callback.OrderIdResolver;
import com.aims.subsystem.vietqr.callback.VietQRCallbackHandler;
import com.aims.subsystem.vietqr.security.ICallbackAuthGuard;


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
 * Controller class to handle VietQR payment processing.
 * Implements IPaymentQRCode interface to generate QR codes and verify payment statuses.
 */
@Service
public class VietQRController implements IPaymentQRCode {

    private static final Logger log = LoggerFactory.getLogger(VietQRController.class);

    private final IVietQRBoundary boundary;
    private final InvoiceRepository invoiceRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderIdResolver orderIdResolver;
    private final String merchantId;
    private final String apiKey;

    public VietQRController(
            IVietQRBoundary boundary,
            InvoiceRepository invoiceRepository,
            DeliveryRepository deliveryRepository,
            OrderIdResolver orderIdResolver,
            @Value("${vietqr.merchant-id:}") String merchantId,
            @Value("${vietqr.api-key:}") String apiKey) {
        this.boundary = boundary;
        this.invoiceRepository = invoiceRepository;
        this.deliveryRepository = deliveryRepository;
        this.orderIdResolver = orderIdResolver;
        this.merchantId = merchantId;
        this.apiKey = apiKey;
    }

    /**
     * Generates a payment QR code from the order information.
     *
     * @param order the order to generate QR code for.
     * @return the generated QRCode object.
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

        QRAccessToken accessToken = boundary.requestAccessToken();

        // Call the boundary for the QR payload; it will fallback if the VietQR API
        // fails.
        String response = boundary.getQRCode(content, amountToPay, orderId, delivery.getRecipientName(), accessToken.getAccessToken());

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

        // Use OrderIdResolver to resolve and restore the canonical order ID
        String rawContent = handler.getRawContent();
        String directOrderId = handler.getDirectOrderId();
        String resolvedOrderId = orderIdResolver.resolve(directOrderId, rawContent);

        String status = handler.getStatus();
        String transactionId = handler.getTransactionId();
        int successFlag = handler.checkSuccess() ? 1 : 0;

        // Construct the system domain entity inside the Controller (DIP compliance)
        return new PaymentResult(
                status,
                "VietQR Callback Payload",
                resolvedOrderId,
                transactionId,
                successFlag
        );
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

    @Override
    public java.util.Map<String, Object> triggerTestCallback(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId is required");
        }

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("invoice is required for order " + orderId));

        long amountToPay = invoice.getSubTotalIncVAT() + invoice.getShippingFee();

        // String OrderId to 13
        String safeOrderId = orderId.replace("-", "");
        if (safeOrderId.length() > 13) {
            safeOrderId = safeOrderId.substring(safeOrderId.length() - 13);
        }
        String content = "Order " + safeOrderId;

        QRAccessToken accessToken = boundary.requestAccessToken();

        try {
            String apiResponse = boundary.callTestCallbackApi(
                content,
                amountToPay,
                "C",
                accessToken.getAccessToken()
            );
            java.util.Map<String, Object> res = new java.util.HashMap<>();
            res.put("status", "SUCCESS");
            res.put("message", "Callback triggered successfully");
            res.put("vietqrResponse", apiResponse);
            return res;
        } catch (Exception e) {
            log.error("Failed to trigger VietQR test callback", e);
            java.util.Map<String, Object> res = new java.util.HashMap<>();
            res.put("status", "FAILED");
            res.put("message", e.getMessage());
            return res;
        }
    }
}

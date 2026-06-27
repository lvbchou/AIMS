package com.aims.gateway;

/**
 * PaymentCaptureParams — gateway-neutral record carrying the parameters
 * required to capture / complete a payment transaction with any payment gateway.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>DIP (Dependency Inversion Principle):</strong> The previous
 * {@link IPaymentGateway#completePayment} signature accepted a full
 * {@code Order} domain entity.  This coupled the payment subsystem to the
 * core domain model.  This record carries only the data the gateway needs:
 * the order identifier and the gateway-issued token.</p>
 *
 * <p>The service layer maps the domain {@code Order} and the token string
 * to this record before calling the gateway.</p>
 *
 * @param orderId      the platform order identifier (used to correlate the
 *                     captured transaction back to the order).
 * @param gatewayToken the token or ID issued by the gateway during payment
 *                     initiation (e.g., PayPal order ID, VietQR reference).
 */
public record PaymentCaptureParams(String orderId, String gatewayToken) {}

package com.aims.gateway;

import java.math.BigDecimal;

/**
 * PaymentInitiateParams — gateway-neutral record carrying the parameters
 * required to create a payment session with any payment gateway.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>DIP (Dependency Inversion Principle):</strong> The previous
 * {@link IPaymentGateway#createPayment} signature accepted a full
 * {@code Invoice} domain entity.  This coupled the payment subsystem to the
 * core domain model — any new gateway implementation had to import domain
 * classes.  This record carries only the primitive payment data the gateway
 * actually needs, breaking that coupling.</p>
 *
 * <p><strong>Principle of Least Knowledge (HFDP Ch. 7):</strong> The gateway
 * does not need to know the full {@code Invoice} structure; it only needs the
 * amount, currency, invoice reference ID, and redirect URLs.</p>
 *
 * <p>The service layer is responsible for mapping the domain {@code Invoice}
 * object to this record before calling the gateway.</p>
 *
 * @param invoiceId  the invoice identifier used as a reference in the gateway order.
 * @param amount     the total amount to charge in the given currency.
 * @param currency   ISO 4217 currency code (e.g., {@code "USD"}, {@code "VND"}).
 * @param returnUrl  the URL to which the gateway redirects the customer on success.
 * @param cancelUrl  the URL to which the gateway redirects the customer on cancellation.
 */
public record PaymentInitiateParams(
        String invoiceId,
        BigDecimal amount,
        String currency,
        String returnUrl,
        String cancelUrl
) {}

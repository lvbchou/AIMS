package com.aims.service;

import com.aims.dto.payment.GatewayTransactionContext;
import com.aims.dto.payment.PaymentCompleteResponse;
import com.aims.exception.PaymentException;

/**
 * IPayThroughPaymentGatewayService — service abstraction for the payment
 * gateway workflow.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>DIP (Dependency Inversion Principle):</strong> Previously,
 * {@link com.aims.controller.PayThroughPaymentGatewayController} depended
 * directly on the concrete {@link PayThroughPaymentGatewayService} class.
 * Controllers should depend on abstractions so that the service can be
 * replaced (e.g., with a mock in tests or a different orchestration strategy)
 * without touching the controller.</p>
 *
 * <p>This interface defines only the two public operations the controller
 * needs, satisfying ISP (Interface Segregation Principle) as well.</p>
 */
public interface IPayThroughPaymentGatewayService {

    /**
     * Initiates a payment session for the given order and returns the gateway
     * context (approval URL + gateway order token).
     *
     * @param orderId the platform order identifier.
     * @return the gateway context containing the redirect URL and token.
     * @throws PaymentException         if the gateway call fails.
     * @throws IllegalArgumentException if {@code orderId} is blank or the
     *                                  order / invoice is not found.
     */
    GatewayTransactionContext createPaymentForOrder(String orderId) throws PaymentException;

    /**
     * Captures / completes the payment for the given gateway token and
     * returns the final payment result.
     *
     * @param token the gateway-issued token received in the redirect callback.
     * @return a {@link PaymentCompleteResponse} describing the outcome.
     * @throws PaymentException if the capture fails or the order is not found.
     */
    PaymentCompleteResponse completePayment(String token) throws PaymentException;
}

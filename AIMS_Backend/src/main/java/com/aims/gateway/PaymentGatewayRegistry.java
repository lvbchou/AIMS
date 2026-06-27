package com.aims.gateway;

import com.aims.entity.PaymentMethod;

/**
 * PaymentGatewayRegistry — abstraction for resolving the correct
 * {@link IPaymentGateway} implementation at runtime from a
 * {@link PaymentMethod} key.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>Strategy Pattern (HFDP Chapter 1):</strong> This registry is the
 * context that selects a payment algorithm (gateway) at runtime without the
 * service needing to know which concrete gateway it is dealing with.  The
 * service holds a reference to the registry, calls
 * {@code getGateway(method)}, and receives whichever {@link IPaymentGateway}
 * strategy is registered for that method.</p>
 *
 * <p><strong>OCP (Open/Closed Principle):</strong> Adding a new payment
 * gateway (MoMo, ZaloPay, Stripe) requires zero changes to the service layer.
 * The new {@link IPaymentGateway} implementation is annotated
 * {@code @Component}; Spring auto-registers it; this registry discovers it
 * automatically.</p>
 *
 * <p>Depending on this interface (rather than the concrete
 * {@link SpringPaymentGatewayRegistry}) keeps callers testable with a mock
 * registry.</p>
 */
public interface PaymentGatewayRegistry {

    /**
     * Resolves the {@link IPaymentGateway} registered for the given payment
     * method.
     *
     * @param method the payment method to look up.
     * @return the matching gateway; never {@code null}.
     * @throws UnsupportedOperationException if no gateway is registered for
     *         the given method.
     */
    IPaymentGateway getGateway(PaymentMethod method);
}

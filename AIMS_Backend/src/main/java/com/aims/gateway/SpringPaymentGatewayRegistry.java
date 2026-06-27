package com.aims.gateway;

import com.aims.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SpringPaymentGatewayRegistry — Spring-managed implementation of
 * {@link PaymentGatewayRegistry} that auto-discovers all
 * {@link IPaymentGateway} beans and indexes them by their supported
 * {@link PaymentMethod}.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>Strategy Pattern (HFDP Chapter 1):</strong> This registry is the
 * mechanism by which the correct strategy (gateway) is selected at runtime.
 * Spring injects every {@code @Component} that implements
 * {@link IPaymentGateway} into the constructor's {@code List} parameter.
 * The registry builds a {@link Map} keyed by {@link PaymentMethod}, so
 * {@link #getGateway(PaymentMethod)} runs in O(1).</p>
 *
 * <p><strong>OCP (Open/Closed Principle):</strong> To register a new gateway
 * (e.g., {@code MoMoController implements IPaymentGateway}), a developer
 * needs only to:</p>
 * <ol>
 *   <li>Create the new class annotated {@code @Component}.</li>
 *   <li>Implement {@link IPaymentGateway#getSupportedMethod()} returning the
 *       appropriate enum value.</li>
 * </ol>
 * <p>No changes to this class, the service, or the controller are required.</p>
 */
@Component
public class SpringPaymentGatewayRegistry implements PaymentGatewayRegistry {

    private final Map<PaymentMethod, IPaymentGateway> gateways;

    /**
     * Constructs the registry from all {@link IPaymentGateway} beans that
     * Spring has collected.
     *
     * @param gatewayList every {@link IPaymentGateway} bean in the application
     *                    context, injected automatically by Spring.
     * @throws IllegalStateException if two gateways declare the same
     *         {@link PaymentMethod}.
     */
    public SpringPaymentGatewayRegistry(List<IPaymentGateway> gatewayList) {
        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(
                        IPaymentGateway::getSupportedMethod,
                        Function.identity(),
                        (existing, duplicate) -> {
                            throw new IllegalStateException(
                                    "Duplicate IPaymentGateway registered for method: "
                                            + existing.getSupportedMethod());
                        }));
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException if no gateway is registered for
     *         the given {@code method}.
     */
    @Override
    public IPaymentGateway getGateway(PaymentMethod method) {
        IPaymentGateway gateway = gateways.get(method);
        if (gateway == null) {
            throw new UnsupportedOperationException(
                    "No IPaymentGateway registered for payment method: " + method);
        }
        return gateway;
    }
}

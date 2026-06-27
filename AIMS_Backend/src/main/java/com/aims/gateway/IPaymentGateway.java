package com.aims.gateway;

import com.aims.dto.payment.GatewayTransactionContext;
import com.aims.dto.payment.GatewayTransactionResult;
import com.aims.entity.PaymentMethod;
import com.aims.exception.PaymentException;

/**
 * IPaymentGateway — the central abstraction for all external payment gateway
 * adapters in the AIMS platform.
 *
 * <h3>Design Patterns Applied</h3>
 * <ul>
 *   <li><strong>Strategy Pattern (HFDP Ch. 1):</strong> Each concrete gateway
 *       ({@code PayPalController}, a future {@code MoMoController}, etc.) is a
 *       concrete strategy.  The service layer holds a reference to this
 *       interface and selects strategies at runtime via
 *       {@link PaymentGatewayRegistry}.</li>
 *   <li><strong>Adapter Pattern (HFDP Ch. 7):</strong> Each implementation
 *       adapts a vendor-specific API to this common interface.</li>
 * </ul>
 *
 * <h3>DIP Improvements</h3>
 * <p>The previous version accepted {@code Invoice} and {@code Order} domain
 * entities directly.  This coupled the payment subsystem to the core domain
 * model.  The updated signatures use gateway-neutral records
 * ({@link PaymentInitiateParams} and {@link PaymentCaptureParams}), making
 * the subsystem independently reusable and microservice-ready.</p>
 *
 * <h3>OCP Compliance</h3>
 * <p>Adding a new payment method requires only creating a new class that
 * implements this interface — no existing files are modified.</p>
 */
public interface IPaymentGateway {

    /**
     * Returns the {@link PaymentMethod} this gateway implementation supports.
     *
     * <p>Used by {@link SpringPaymentGatewayRegistry} to build the
     * {@code PaymentMethod → IPaymentGateway} lookup map.</p>
     *
     * @return the payment method handled by this gateway.
     */
    PaymentMethod getSupportedMethod();

    /**
     * Initiates a payment session with the external gateway and returns a
     * context object containing the approval URL and the gateway-issued order
     * token.
     *
     * @param params gateway-neutral payment parameters; never {@code null}.
     * @return a {@link GatewayTransactionContext} with the approval URL and
     *         gateway order identifier.
     * @throws PaymentException if the gateway call fails.
     */
    GatewayTransactionContext createPayment(PaymentInitiateParams params) throws PaymentException;

    /**
     * Captures / completes a previously initiated payment transaction.
     *
     * @param params gateway-neutral capture parameters; never {@code null}.
     * @return a {@link GatewayTransactionResult} describing the capture outcome.
     * @throws PaymentException if the gateway call fails.
     */
    GatewayTransactionResult completePayment(PaymentCaptureParams params) throws PaymentException;
}

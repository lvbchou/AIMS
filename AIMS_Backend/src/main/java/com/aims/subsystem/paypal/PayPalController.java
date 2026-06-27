// Coupling Level: Stamp Coupling
// Cohesion Level: Sequential Cohesion
// Reason for Coupling: Implements IPaymentGateway and receives gateway-neutral
//   PaymentInitiateParams / PaymentCaptureParams rather than domain entities,
//   breaking the previous stamp coupling to Invoice and Order.
// Reason for Cohesion: The methods coordinate a sequence of steps (auth → convert
//   → boundary call → parse → return) where each step's output feeds the next.
/**
 * SOLID Principles Analysis (refactored):
 * - **SRP**: Token lifecycle delegated to {@link PayPalAuthManager}. Currency
 *   conversion is an injected collaborator. Parsing is delegated to
 *   {@link PayPalResponseMapper}. This class is now responsible solely for
 *   payment-flow orchestration.
 * - **DIP**: All collaborators ({@link PayPalBoundary}, {@link CurrencyConverter},
 *   {@link PayPalAuthManager}) are injected via the constructor — no {@code new}
 *   inside business logic.
 * - **OCP**: Adding a second gateway (MoMo, Stripe) does NOT require modifying
 *   this class. The {@link SpringPaymentGatewayRegistry} discovers it automatically.
 * - **LSP**: Implements {@link com.aims.gateway.IPaymentGateway} fully.
 * - **ISP**: The interface is minimal (3 methods); no unused methods forced on this class.
 *
 * <h3>Changes in this revision</h3>
 * <ul>
 *   <li><strong>R5:</strong> Replaced scattered magic string literals
 *       ({@code "MOCK-TOKEN-"}, {@code "MOCK-EC-"}) with
 *       {@link PayPalMockMode} constants.</li>
 *   <li><strong>R2:</strong> Removed the static {@code extractTokenFromApprovalUrl()}
 *       method. The service layer now reads the token directly from
 *       {@link com.aims.dto.GatewayTransactionContext#getGatewayOrderId()}, which this
 *       class always populates from the PayPal order ID returned by the API.</li>
 * </ul>
 */
package com.aims.subsystem.paypal;

import com.aims.dto.payment.GatewayTransactionContext;
import com.aims.dto.payment.GatewayTransactionResult;
import com.aims.dto.GatewayRefundResult;
import com.aims.entity.PaymentMethod;
import com.aims.exception.PaymentException;
import com.aims.gateway.IRefundableGateway;
import com.aims.gateway.PaymentCaptureParams;
import com.aims.gateway.PaymentInitiateParams;
import com.aims.gateway.PaymentRefundParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * PayPalController — the Adapter/Facade for the PayPal payment subsystem.
 *
 * <h3>Design Patterns</h3>
 * <ul>
 *   <li><strong>Adapter (HFDP Ch. 7):</strong> Adapts the PayPal REST API
 *       (accessed via {@link PayPalBoundary}) to the {@link IRefundableGateway}
 *       interface expected by the service layer.</li>
 *   <li><strong>Facade (HFDP Ch. 7):</strong> Presents a simple two-method
 *       interface to the outside world, hiding OAuth token management, currency
 *       conversion, boundary calls, and DTO parsing happening underneath.</li>
 *   <li><strong>Strategy (HFDP Ch. 1):</strong> Acts as one concrete strategy
 *       in the {@link com.aims.gateway.SpringPaymentGatewayRegistry} registry;
 *       selected at runtime when {@code PaymentMethod.PAYPAL} is the active
 *       payment method.</li>
 * </ul>
 */
@Component
public class PayPalController implements IRefundableGateway {

    private final String returnUrl;
    private final String cancelUrl;

    private final PayPalBoundary payPalBoundary;
    private final CurrencyConverter currencyConverter;
    private final PayPalAuthManager authManager;

    /**
     * Constructs the PayPal gateway adapter with all collaborators injected.
     *
     * <p><strong>DIP compliance:</strong> no {@code new} here — all
     * infrastructure objects arrive via Spring constructor injection declared
     * in {@link com.aims.subsystem.paypal.config.PayPalConfig}.</p>
     *
     * @param returnUrl         PayPal redirect URL on successful payment.
     * @param cancelUrl         PayPal redirect URL on cancelled payment.
     * @param payPalBoundary    the raw HTTP client for PayPal REST API calls.
     * @param currencyConverter the VND→USD converter strategy.
     * @param authManager       the OAuth token lifecycle manager.
     */
    public PayPalController(
            @Value("${paypal.url.return}") String returnUrl,
            @Value("${paypal.url.cancel}") String cancelUrl,
            PayPalBoundary payPalBoundary,
            CurrencyConverter currencyConverter,
            PayPalAuthManager authManager) {

        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.payPalBoundary = payPalBoundary;
        this.currencyConverter = currencyConverter;
        this.authManager = authManager;
    }

    // -------------------------------------------------------------------------
    // IPaymentGateway implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link PaymentMethod#PAYPAL} so that
     * {@link com.aims.gateway.SpringPaymentGatewayRegistry} can index this
     * bean automatically.</p>
     */
    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.PAYPAL;
    }

    /**
     * Initiates a PayPal payment session.
     *
     * <p>Retrieves a valid OAuth token via {@link PayPalAuthManager}, converts
     * the VND amount to USD via {@link CurrencyConverter}, creates a PayPal
     * order via {@link PayPalBoundary}, and returns the approval URL + order ID
     * wrapped in a {@link GatewayTransactionContext}.</p>
     *
     * <p><strong>Contract (R2):</strong> This method always populates
     * {@link GatewayTransactionContext#getGatewayOrderId()} with the PayPal
     * order ID directly. The service layer can therefore read the token from
     * {@code context.getGatewayOrderId()} without any URL parsing.</p>
     *
     * <p><strong>Mock mode:</strong> When {@code clientId} starts with
     * {@link PayPalMockMode#CLIENT_ID_PREFIX}, the boundary is never called
     * and a synthetic context is returned immediately.</p>
     *
     * @param params gateway-neutral initiation parameters (amount, currency,
     *               invoiceId, redirect URLs).
     * @return a context with the PayPal order ID and approval URL.
     * @throws PaymentException if the gateway call fails.
     */
    @Override
    public GatewayTransactionContext createPayment(PaymentInitiateParams params) throws PaymentException {
        try {
            String token = authManager.getValidToken();

            // Developer Mock Mode — sentinel token signals mock mode (R5)
            if (token != null && token.startsWith(PayPalMockMode.TOKEN_PREFIX)) {
                String mockOrderId = PayPalMockMode.ORDER_ID_PREFIX + System.currentTimeMillis();
                String mockApprovalUrl = this.returnUrl + "?token=" + mockOrderId;
                return new GatewayTransactionContext(mockOrderId, mockApprovalUrl);
            }

            // Convert VND → USD
            BigDecimal usdAmount = currencyConverter.convert(params.amount(), "VND", "USD");

            // Build and send the create-order request
            CreateOrderRequest request = new CreateOrderRequest(
                    usdAmount.toString(),
                    "USD",
                    params.invoiceId(),
                    this.returnUrl,
                    this.cancelUrl);

            String responseJson = payPalBoundary.createOrder(token, request);
            CreateOrderResponse response = PayPalResponseMapper.parseCreateOrder(responseJson);

            // gatewayOrderId is always populated here — service layer reads it directly (R2)
            return new GatewayTransactionContext(response.getPaypalOrderId(), response.getApproveUrl());

        } catch (PaymentException pe) {
            throw pe;
        } catch (Exception e) {
            throw new PaymentException("Failed to create PayPal payment: " + e.getMessage());
        }
    }

    /**
     * Captures a previously initiated PayPal payment.
     *
     * @param params gateway-neutral capture parameters (orderId, gatewayToken).
     * @return the capture result with transaction ID and success flag.
     * @throws PaymentException if the capture call fails.
     */
    @Override
    public GatewayTransactionResult completePayment(PaymentCaptureParams params) throws PaymentException {
        String paypalOrderId = params.gatewayToken();

        // Developer Mock Mode — mock order IDs start with the sentinel prefix (R5)
        if (paypalOrderId != null && paypalOrderId.startsWith(PayPalMockMode.ORDER_ID_PREFIX)) {
            String transactionId = "MOCK-TX-" + System.currentTimeMillis();
            return new GatewayTransactionResult(transactionId, params.orderId(),
                    "COMPLETED", true, "Mock capture succeeded");
        }

        try {
            String token = authManager.getValidToken();
            String responseJson = payPalBoundary.captureOrder(token, paypalOrderId);
            CaptureOrderResponse response = PayPalResponseMapper.parseCaptureOrder(responseJson);

            boolean success = response.checkSuccess();
            String status = success ? response.getStatus() : response.getErrorName();
            String message = success ? null : response.getErrorMessage();

            return new GatewayTransactionResult(
                    response.getTransactionId(), params.orderId(), status, success, message);

        } catch (PaymentException pe) {
            throw pe;
        } catch (Exception e) {
            throw new PaymentException("Failed to complete PayPal payment: " + e.getMessage());
        }
    }

    /**
     * Refunds a previously captured PayPal payment transaction.
     *
     * @param params gateway-neutral refund parameters containing the transaction ID.
     * @return the refund result indicating success or failure.
     * @throws PaymentException if the gateway call fails.
     */
    @Override
    public GatewayRefundResult refundPayment(PaymentRefundParams params) throws PaymentException {
        String captureId = params.transactionId();

        // Developer Mock Mode — mock order/transaction IDs start with MOCK- or MOCK-TX-
        if (captureId != null && (captureId.startsWith("MOCK-") || captureId.startsWith("MOCK-TX-"))) {
            String mockRefundId = "MOCK-REF-" + System.currentTimeMillis();
            return GatewayRefundResult.builder()
                    .refundId(mockRefundId)
                    .status("COMPLETED")
                    .success(true)
                    .message("Mock refund succeeded")
                    .build();
        }

        try {
            String token = authManager.getValidToken();
            String responseJson = payPalBoundary.refundCapture(token, captureId);
            RefundResponse response = PayPalResponseMapper.parseRefund(responseJson);

            boolean success = response.checkSuccess();
            String status = success ? response.getStatus() : response.getErrorName();
            String message = success ? null : response.getErrorMessage();

            return GatewayRefundResult.builder()
                    .refundId(response.getRefundId())
                    .status(status)
                    .success(success)
                    .message(message)
                    .build();

        } catch (PaymentException pe) {
            throw pe;
        } catch (Exception e) {
            throw new PaymentException("Failed to refund PayPal payment: " + e.getMessage());
        }
    }
}

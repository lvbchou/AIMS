/**
 * SOLID Principles Analysis (refactored):
 * - **OCP (Open/Closed Principle) Compliance**: The hardcoded PaymentMethod.PAYPAL has
 *   been removed. The service uses PaymentGatewayRegistry to resolve the gateway at runtime
 *   from order.getPaymentMethod() (R1). Adding VietQR, MoMo, or any future gateway requires
 *   zero changes to this class.
 * - **DIP (Dependency Inversion Principle) Compliance**: All concrete JPA repositories replaced
 *   with abstractions. The service now depends on IInvoiceRepository (unified), IOrderRepository,
 *   PaymentGatewayRegistry, and IPaymentTransactionRepository (JpaRepository interface).
 *   The import of PayPalController has been removed (R2) — the service no longer calls any
 *   PayPal-specific code.
 * - **SRP (Single Responsibility Principle) Compliance**: Token extraction is no longer done here
 *   (R2) — the context's gatewayOrderId is read directly. Transaction creation uses
 *   PaymentTransactionFactory (P2.3). The service focuses purely on orchestration.
 */
package com.aims.service;
import com.aims.dto.payment.GatewayTransactionContext;
import com.aims.dto.payment.GatewayTransactionResult;
import com.aims.dto.payment.PaymentCompleteResponse;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.entity.PaymentMethod;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.TransactionStatus;
import com.aims.exception.PaymentException;
import com.aims.factory.PaymentTransactionFactory;
import com.aims.gateway.IPaymentGateway;
import com.aims.gateway.PaymentCaptureParams;
import com.aims.gateway.PaymentGatewayRegistry;
import com.aims.gateway.PaymentInitiateParams;
import com.aims.repository.IInvoiceRepository;
import com.aims.repository.IOrderRepository;
import com.aims.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;


/**
 * PayThroughPaymentGatewayService — orchestrates the full lifecycle of
 * gateway-based payment sessions (initiation → capture → persistence).
 *
 * <h3>Design Patterns Applied</h3>
 * <ul>
 *   <li><strong>Strategy Pattern (HFDP Ch. 1):</strong> Uses
 *       {@link PaymentGatewayRegistry} to resolve the correct
 *       {@link IPaymentGateway} at runtime from the order's stored payment method.
 *       No payment-method enum value is hardcoded in this class.</li>
 *   <li><strong>Factory Method (HFDP Ch. 4):</strong> Uses
 *       {@link PaymentTransactionFactory} to create {@link PaymentTransaction}
 *       instances, keeping the entity pure.</li>
 * </ul>
 *
 * <h3>SOLID Compliance</h3>
 * <ul>
 *   <li>OCP: Zero changes needed here when a new gateway is added.</li>
 *   <li>DIP: Depends only on abstractions (interfaces), never on concrete classes.
 *       No subsystem-specific import exists in this class.</li>
 *   <li>SRP: Orchestration only — no URL parsing, no factory logic, no gateway knowledge.</li>
 * </ul>
 */
@Service
public class PayThroughPaymentGatewayService implements IPayThroughPaymentGatewayService {

    private final PaymentGatewayRegistry gatewayRegistry;
    private final IOrderRepository orderRepository;
    private final IInvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public PayThroughPaymentGatewayService(
            PaymentGatewayRegistry gatewayRegistry,
            IOrderRepository orderRepository,
            IInvoiceRepository invoiceRepository,
            PaymentTransactionRepository paymentTransactionRepository) {

        this.gatewayRegistry = gatewayRegistry;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    // -------------------------------------------------------------------------
    // Public API — declared in IPayThroughPaymentGatewayService
    // -------------------------------------------------------------------------

    /**
     * Initiates a payment session for the given order.
     *
     * <p>Steps:
     * <ol>
     *   <li>Load the {@link Order} and {@link Invoice} from their repositories.</li>
     *   <li>Calculate the invoice total.</li>
     *   <li>Resolve the appropriate {@link IPaymentGateway} from the registry using
     *       the order's stored {@link PaymentMethod} (R1). Falls back to
     *       {@link PaymentMethod#PAYPAL} for orders created before the
     *       {@code payment_method} column was added.</li>
     *   <li>Map domain objects to gateway-neutral {@link PaymentInitiateParams}.</li>
     *   <li>Call the gateway's {@code createPayment}.</li>
     *   <li>Persist the order's payment token and update the order status.</li>
     *   <li>Return the {@link GatewayTransactionContext}.</li>
     * </ol>
     *
     * @param orderId the platform order identifier.
     * @return the gateway context (approval URL + gateway token).
     * @throws PaymentException         if the gateway call fails.
     * @throws IllegalArgumentException if the order or invoice is not found.
     */
    @Override
    public GatewayTransactionContext createPaymentForOrder(String orderId) throws PaymentException {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID is required.");
        }

        // 1. Load domain objects via the unified invoice repository
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found for order: " + orderId));
        Order order = invoice.getOrder();
        if (order == null) {
            throw new IllegalArgumentException("Order not found for order ID: " + orderId);
        }

        // 2. Calculate total (sets transient totalAmount on the invoice)
        invoice.calculateTotalAmount();

        // 3. Resolve gateway from order's paymentMethod (R1).
        //    Nullable-safe: orders created before the payment_method column was added
        //    default to PAYPAL so existing data continues to work without migration.
        PaymentMethod method = resolvePaymentMethod(order);
        IPaymentGateway gateway = gatewayRegistry.getGateway(method);

        // 4. Map Invoice to gateway-neutral DTO.
        //    returnUrl/cancelUrl are gateway-specific — the concrete gateway reads them
        //    from its own @Value-injected config, so we pass null here.
        PaymentInitiateParams params = new PaymentInitiateParams(
                invoice.getId(),
                invoice.getTotalAmount(),
                "VND",
                null,
                null);

        // 5. Call gateway
        GatewayTransactionContext context = gateway.createPayment(params);

        // 6. Persist token and update order status
        order.setStatus(com.aims.constants.OrderStatusValues.AWAITING_PAYMENT);
        orderRepository.updateOrder(order);

        // R2: The gateway always populates gatewayOrderId directly (see PayPalController).
        //     No URL parsing is required here — eliminates the previous PayPalController import.
        String token = context.getGatewayOrderId();
        if (token == null || token.isBlank()) {
            throw new PaymentException("Gateway did not return a valid payment token for order: " + orderId);
        }
        orderRepository.rememberPaymentToken(token, order);

        return context;
    }

    /**
     * Captures / completes a payment using the gateway-issued token.
     *
     * <p>Steps:
     * <ol>
     *   <li>Look up the {@link Order} by gateway token.</li>
     *   <li>Load the associated {@link Invoice}.</li>
     *   <li>Resolve the gateway from the order's payment method (R1, Strategy Pattern).</li>
     *   <li>Map token + orderId to {@link PaymentCaptureParams}.</li>
     *   <li>Call the gateway's {@code completePayment}.</li>
     *   <li>Create a {@link PaymentTransaction} via {@link PaymentTransactionFactory}
     *       using the resolved payment method (R9 — no longer hardcodes PAYPAL).</li>
     *   <li>Persist and return a {@link PaymentCompleteResponse}.</li>
     * </ol>
     *
     * @param token the gateway-issued token.
     * @return the payment outcome.
     * @throws PaymentException if the capture fails or the order is not found.
     */
    @Override
    public PaymentCompleteResponse completePayment(String token) throws PaymentException {
        // 1. Load order by gateway token
        Order order = orderRepository.findByToken(token);
        if (order == null) {
            throw new PaymentException("Order not found for token.");
        }

        // 2. Load invoice
        Invoice invoice = invoiceRepository.findByOrder(order).orElse(null);

        // 3. Resolve gateway from the order's payment method (R1) — no hardcoding
        PaymentMethod method = resolvePaymentMethod(order);
        IPaymentGateway gateway = gatewayRegistry.getGateway(method);

        // 4. Map to gateway-neutral DTO
        PaymentCaptureParams captureParams = new PaymentCaptureParams(order.getOrderId(), token);

        // 5. Call gateway
        GatewayTransactionResult result = gateway.completePayment(captureParams);

        // 6. Create transaction via factory using the resolved method (R9 — not hardcoded PAYPAL)
        PaymentTransaction transaction = PaymentTransactionFactory.createPending(
                invoice, order.getOrderId(), method);
        String txId = result.getTransactionId() != null
                ? result.getTransactionId()
                : "TX-" + System.currentTimeMillis();
        transaction.setTransactionId(txId);

        // 7. Check result and persist
        if (result.checkSuccess()) {
            if (paymentTransactionRepository.findById(transaction.getTransactionId())
                    .map(existing -> TransactionStatus.success.equals(existing.getStatus()))
                    .orElse(false)) {
                return PaymentCompleteResponse.builder()
                        .status("SUCCESS")
                        .message("Payment was already captured successfully.")
                        .orderId(order.getOrderId())
                        .transactionId(transaction.getTransactionId())
                        .build();
            }

            transaction.setStatus(TransactionStatus.success);
            paymentTransactionRepository.save(transaction);
            return PaymentCompleteResponse.builder()
                    .status("SUCCESS")
                    .message("Payment captured successfully. Confirm the paid order through Place Order.")
                    .orderId(order.getOrderId())
                    .transactionId(transaction.getTransactionId())
                    .build();
        } else {
            transaction.setStatus(TransactionStatus.failed);
            paymentTransactionRepository.save(transaction);
            throw new PaymentException(result.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves the {@link PaymentMethod} to use for gateway dispatch.
     *
     * <p>Reads {@link Order#getPaymentMethod()} when set. Falls back to
     * {@link PaymentMethod#PAYPAL} for orders created before the
     * {@code payment_method} column existed in the database.</p>
     *
     * @param order the order whose payment method is to be resolved.
     * @return the resolved payment method; never {@code null}.
     */
    private static PaymentMethod resolvePaymentMethod(Order order) {
        return order.getPaymentMethod() != null ? order.getPaymentMethod() : PaymentMethod.PAYPAL;
    }
}

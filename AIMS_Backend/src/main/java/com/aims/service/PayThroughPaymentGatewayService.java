/**
 * SOLID Principles Analysis:
 * - **OCP (Open/Closed Principle) Violation**: The service has hardcoded dependencies on `PaymentMethod.PAYPAL` inside `completePayment()`. Supporting a new payment gateway would require modifying this core service logic rather than just extending it.
 * - **DIP (Dependency Inversion Principle) Violation**: The class depends directly on concrete repositories like `JpaInvoiceRepository` and `PaymentTransactionRepository` instead of abstract repository interfaces.
 * 
 * **Improvement Direction**:
 * 1. Inject a strategy-based factory or resolve the payment method dynamically from the gateway result rather than hardcoding `PaymentMethod.PAYPAL`.
 * 2. Replace concrete repositories with abstraction interfaces to decouple persistence mechanisms.
 */
package com.aims.service;

import com.aims.IPaymentGateway;
import com.aims.dto.GatewayTransactionContext;
import com.aims.dto.GatewayTransactionResult;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.entity.PaymentMethod;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.TransactionStatus;
import com.aims.exception.PaymentException;
import com.aims.repository.IOrderRepository;
import com.aims.repository.JpaInvoiceRepository;
import com.aims.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;

/**
 * PayThroughPaymentGatewayService - manages the workflow of initiating and
 * completing payments.
 * It is fully integrated with JPA to persist Invoices, Orders, and
 * PaymentTransaction logs to PostgreSQL.
 */
@Service
public class PayThroughPaymentGatewayService {

    private final IPaymentGateway paymentGateway;
    private final IOrderRepository orderRepository;
    private final JpaInvoiceRepository jpaInvoiceRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public PayThroughPaymentGatewayService(
            IPaymentGateway paymentGateway,
            IOrderRepository orderRepository,
            JpaInvoiceRepository jpaInvoiceRepository,
            PaymentTransactionRepository paymentTransactionRepository) {

        this.paymentGateway = paymentGateway;
        this.orderRepository = orderRepository;
        this.jpaInvoiceRepository = jpaInvoiceRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    /**
     * Initiates the payment session with the gateway and saves the Invoice/Order
     * database record.
     */
    public String createPayment(Invoice invoice) throws PaymentException {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice cannot be null");
        }

        // 1. Initiate payment session via external gateway
        GatewayTransactionContext context = paymentGateway.createPayment(invoice);

        // 2. Update order status and save invoice/order record to PostgreSQL database
        Order order = invoice.getOrder();
        if (order != null) {
            order.setStatus("pending");
        }

        // Saving invoice will cascade and automatically save/update the Order record
        jpaInvoiceRepository.save(invoice);

        return context.getApprovalUrl();
    }

    /**
     * Captures/executes the payment via the gateway, updates Order status to PAID,
     * and saves a PaymentTransaction log.
     */
    public void completePayment(String token) throws PaymentException {
        // 1. Look up the order using the token
        Order order = orderRepository.findByToken(token);
        if (order == null) {
            throw new PaymentException("Order not found for token.");
        }

        // 2. Fetch the invoice associated with this order from database
        Invoice invoice = jpaInvoiceRepository.findByOrder(order).orElse(null);

        // 3. Complete and capture the payment via external gateway
        GatewayTransactionResult result = paymentGateway.completePayment(order, token);

        // 4. Instantiate the PaymentTransaction record
        long txAmount = (invoice != null) ? invoice.calculateTotalAmount() : 0;
        PaymentTransaction transaction = new PaymentTransaction(
                result.getTransactionId() != null ? result.getTransactionId() : "TX-" + System.currentTimeMillis(),
                txAmount,
                PaymentMethod.PAYPAL,
                "PayPal payment processed for Order ID: " + order.getOrderId());
        transaction.setInvoice(invoice);

        // 5. Check result and update status
        if (result.checkSuccess()) {
            order.setStatus("approved");
            orderRepository.updateOrder(order); // Save updated Order state

            transaction.setStatus(TransactionStatus.success);
            paymentTransactionRepository.save(transaction); // Persist successful transaction to PostgreSQL database
        } else {
            transaction.setStatus(TransactionStatus.failed);
            paymentTransactionRepository.save(transaction); // Persist failed transaction log to PostgreSQL database

            throw new PaymentException(result.getMessage());
        }
    }
}

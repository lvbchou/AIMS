// Coupling Level: Stamp Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: The method createPayment accepts an entire Invoice object but only needs a subset of its fields (like total amount and ID). The method completePayment accepts an entire Order object but only needs the order ID.
// Reason for Cohesion: The interface defines a highly focused abstraction of payment operations, where all defined methods serve the single purpose of processing standard gateway payments.
/**
 * SOLID Principles Analysis:
 * - **DIP (Dependency Inversion Principle) Violation**: The interface methods depend on high-level domain entities (`Invoice` and `Order`). A generic gateway abstraction should be independent of domain models and instead accept generic DTOs or primitive parameters (e.g., amount, order ID, token).
 * - **ISP (Interface Segregation Principle) Adherence**: The interface is minimal, highly-focused, and defines only necessary methods for payment processing.
 * 
 * **Improvement Direction**: Refactor the methods to accept generic payment parameter objects (e.g., `PaymentInitiateRequest` or simple fields) to decouple the payment subsystem from specific domain entities like `Invoice` or `Order`.
 */
package com.aims;

import com.aims.dto.GatewayTransactionContext;
import com.aims.dto.GatewayTransactionResult;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.exception.PaymentException;

public interface IPaymentGateway {
    public GatewayTransactionContext createPayment(Invoice invoice) throws PaymentException;

    public GatewayTransactionResult completePayment(Order order, String paymentId) throws PaymentException;
}

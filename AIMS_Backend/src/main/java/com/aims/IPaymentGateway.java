// Coupling Level: Stamp Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: The method createPayment accepts an entire Invoice object but only needs a subset of its fields (like total amount and ID). The method completePayment accepts an entire Order object but only needs the order ID.
// Reason for Cohesion: The interface defines a highly focused abstraction of payment operations, where all defined methods serve the single purpose of processing standard gateway payments.
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

package com.aims.gateway;

import com.aims.dto.GatewayRefundResult;
import com.aims.exception.PaymentException;

/**
 * IRefundableGateway — extends IPaymentGateway to support refund processing.
 * Following the Interface Segregation Principle (ISP), only gateways that support
 * refunds will implement this interface.
 */
public interface IRefundableGateway extends IPaymentGateway {

    /**
     * Refunds a previously captured payment transaction.
     *
     * @param params the gateway-neutral refund parameters.
     * @return the result of the refund operation.
     * @throws PaymentException if the gateway call fails.
     */
    GatewayRefundResult refundPayment(PaymentRefundParams params) throws PaymentException;
}

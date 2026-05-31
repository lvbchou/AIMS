package com.aims.subsystem;

import com.aims.entity.Order;
import com.aims.entity.PaymentResult;
import com.aims.entity.QRCode;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This interface exposes a narrow payment contract and keeps callers dependent
 * on data-shaped inputs rather than VietQR implementation details.
 *
 * @author Team 03
 * @since 1.0.0
 */
public interface IPaymentQRCode {

    /**
     * Builds the payment QR payload for the given order.
     *
     * @param order source order used to build the QR payload.
     * @return a {@link QRCode} containing the QR data ready for display.
     */
    QRCode getQRCode(Order order);

    /**
     * Checks the payment status from a raw VietQR callback payload.
     *
     * @param callbackData raw callback payload sent by VietQR.
     * @return the parsed and verified callback result.
     */
    PaymentResult checkPaymentStatus(String callbackData);
}
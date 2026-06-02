package com.aims.subsystem;

import com.aims.entity.Order;
import com.aims.entity.PaymentResult;
import com.aims.entity.QRCode;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 *
 * This interface exposes a narrow payment contract and keeps callers dependent
 * on data-shaped inputs rather than VietQR implementation details.
 *
 * SOLID: Single Responsibility Principle (SRP) - Not Violated
 *
 * This interface defines a focused contract for QR-code-based payment operations.
 *
 * SOLID: Open/Closed Principle (OCP) - Not Violated
 *
 * New QR payment providers can implement this interface without modifying
 * existing code. The interface enables extension through polymorphism.
 *
 * SOLID: Liskov Substitution Principle (LSP) - Not Violated
 *
 * Any implementation of this interface can replace another without affecting
 * the correctness of the calling code, as long as the contract is honored.
 *
 * SOLID VIOLATION: Interface Segregation Principle (ISP)
 *
 * Problem: This interface bundles two conceptually distinct operations:
 *   1. getQRCode(Order) — QR code generation (outbound: system to user)
 *   2. checkPaymentStatus(String) — callback verification (inbound: external to system)
 *   A payment provider that only generates QR codes (e.g. a static QR provider)
 *   would still be forced to implement checkPaymentStatus.
 * Impact: Implementations that only handle one direction of the payment flow
 *   must provide empty or throwing implementations for the other method.
 * Improvement:
 *   - Split into IQRCodeGenerator with getQRCode(Order)
 *   - Split into IPaymentCallbackVerifier with checkPaymentStatus(String)
 *   - Implementations can implement one or both interfaces as needed
 *
 * SOLID: Dependency Inversion Principle (DIP) - Not Violated
 *
 * This IS an abstraction. High-level modules (PayOrderService) correctly
 * depend on this interface rather than on concrete VietQR implementation
 * classes. This is a good example of DIP compliance.
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
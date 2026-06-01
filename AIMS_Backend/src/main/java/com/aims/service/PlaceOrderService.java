/*
 * LAB12 SOLID REVIEW:
 * Violated principles:
 * - ISP: one interface exposes all steps of the Place Order workflow
 *   (cart validation, shipping calculation, invoice creation, paid-order
 *   confirmation). A caller that only needs shipping still depends on invoice
 *   and confirmation operations.
 * - DIP: the use-case service abstraction depends directly on HttpSession,
 *   which is a web-framework detail. This couples application logic to the
 *   servlet layer and makes tests/reuse outside HTTP harder.
 * Impact: the contract is broad, changes in one workflow step can ripple to
 * unrelated clients, and the domain use case is harder to test without servlet
 * infrastructure.
 * Improvement direction: split into smaller ports such as CartValidationService,
 * ShippingFeeService, InvoiceCreationService, and PaidOrderConfirmationService;
 * pass a domain Cart/CartContext instead of HttpSession.
 */
package com.aims.service;

import com.aims.dto.request.DeliveryInfoRequest;
import com.aims.dto.request.PlaceOrderRequest;
import com.aims.dto.response.InvoiceResponse;
import jakarta.servlet.http.HttpSession;

public interface PlaceOrderService {
    void processOrder(PlaceOrderRequest placeOrderRequest, HttpSession session);

    Long calculateShippingFee(DeliveryInfoRequest deliveryInfoRequest, HttpSession session);

    InvoiceResponse createInvoice(DeliveryInfoRequest deliveryInfoRequest, HttpSession session);

    InvoiceResponse confirmPaidOrder(String orderId, HttpSession session);
}

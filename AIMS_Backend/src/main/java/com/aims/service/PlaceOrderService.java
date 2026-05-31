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

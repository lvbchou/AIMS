package com.aims.service;

import com.aims.dto.InvoiceScreenDTO;
import com.aims.dto.request.CalculateShippingRequest;
import com.aims.dto.request.CreateInvoiceRequest;
import com.aims.dto.request.PlaceOrderRequest;
import com.aims.dto.response.InvoiceResponse;
import com.aims.service.placeorder.CheckoutCartService;
import com.aims.service.placeorder.CheckoutShippingFeeService;
import com.aims.service.placeorder.InvoiceCreationService;
import com.aims.service.placeorder.InvoiceQueryService;
import com.aims.service.placeorder.PaidOrderConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceOrderService {
    private final CheckoutCartService checkoutCartService;
    private final CheckoutShippingFeeService checkoutShippingFeeService;
    private final InvoiceCreationService invoiceCreationService;
    private final InvoiceQueryService invoiceQueryService;
    private final PaidOrderConfirmationService paidOrderConfirmationService;

    public void processOrder(PlaceOrderRequest placeOrderRequest) {
        checkoutCartService.validateAndBuildCartContext(
                placeOrderRequest == null ? null : placeOrderRequest.getItems());
    }

    public Long calculateShippingFee(CalculateShippingRequest request) {
        return checkoutShippingFeeService.calculateShippingFee(request);
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        return invoiceCreationService.createInvoice(request);
    }

    public InvoiceScreenDTO getInvoiceScreen(String orderId) {
        return invoiceQueryService.getInvoiceScreen(orderId);
    }

    public InvoiceResponse confirmPaidOrder(String orderId) {
        return paidOrderConfirmationService.confirmPaidOrder(orderId);
    }
}

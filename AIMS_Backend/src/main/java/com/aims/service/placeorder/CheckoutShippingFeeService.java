package com.aims.service.placeorder;

import com.aims.dto.order.CalculateShippingRequest;
import com.aims.exception.InvalidOrderException;
import com.aims.service.shipping.ShippingFeeRequest;
import com.aims.service.shipping.ShippingFeeResult;
import com.aims.service.shipping.ShippingFeeService;
import org.springframework.stereotype.Service;

@Service
public class CheckoutShippingFeeService {
    private final CheckoutCartService checkoutCartService;
    private final ShippingFeeService shippingFeeService;

    public CheckoutShippingFeeService(
            CheckoutCartService checkoutCartService,
            ShippingFeeService shippingFeeService) {
        this.checkoutCartService = checkoutCartService;
        this.shippingFeeService = shippingFeeService;
    }

    public Long calculateShippingFee(CalculateShippingRequest request) {
        if (request == null || request.getDeliveryProvince() == null
                || request.getDeliveryProvince().trim().isEmpty()) {
            throw new InvalidOrderException("Delivery province is required to calculate shipping fee.");
        }

        CartValidationResult cart = checkoutCartService.validateAndBuildCartContext(request.getItems());
        ShippingFeeResult shippingFee = shippingFeeService.calculate(new ShippingFeeRequest(
                request.getDeliveryProvince(),
                cart.subtotalExVat(),
                cart.shippingItems()));
        return shippingFee.shippingFee();
    }
}

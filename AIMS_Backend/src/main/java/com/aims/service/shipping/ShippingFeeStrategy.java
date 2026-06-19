package com.aims.service.shipping;

public interface ShippingFeeStrategy {
    String getStrategyCode();

    ShippingFeeResult calculate(ShippingFeeRequest request);
}

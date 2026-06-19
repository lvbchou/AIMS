package com.aims.service.shipping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShippingFeeService {
    private final List<ShippingFeeStrategy> strategies;
    private final String defaultStrategyCode;

    public ShippingFeeService(
            List<ShippingFeeStrategy> strategies,
            @Value("${aims.shipping.strategy}") String defaultStrategyCode) {
        this.strategies = strategies;
        this.defaultStrategyCode = defaultStrategyCode;
    }

    public ShippingFeeResult calculate(ShippingFeeRequest request) {
        ShippingFeeStrategy strategy = strategies.stream()
                .filter(candidate -> candidate.getStrategyCode().equalsIgnoreCase(defaultStrategyCode))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No shipping fee strategy configured for code: " + defaultStrategyCode));
        return strategy.calculate(request);
    }
}

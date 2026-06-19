package com.aims.service.shipping;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ActualWeightShippingFeeStrategy implements ShippingFeeStrategy {
    private static final String STRATEGY_CODE = "ACTUAL_WEIGHT";
    private static final long FREE_SHIPPING_THRESHOLD = 100_000L;
    private static final long MAX_FREE_SHIPPING_DISCOUNT = 25_000L;
    private static final long INNER_CITY_BASE_FEE = 22_000L;
    private static final long OTHER_PROVINCE_BASE_FEE = 30_000L;
    private static final double INNER_CITY_BASE_WEIGHT = 3.0;
    private static final double OTHER_PROVINCE_BASE_WEIGHT = 0.5;
    private static final double EXTRA_WEIGHT_STEP = 0.5;
    private static final long EXTRA_STEP_FEE = 2_500L;

    @Override
    public String getStrategyCode() {
        return STRATEGY_CODE;
    }

    @Override
    public ShippingFeeResult calculate(ShippingFeeRequest request) {
        if (request == null || request.deliveryProvince() == null
                || request.deliveryProvince().trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery province is required to calculate shipping fee.");
        }

        // Current AIMS policy charges by actual product weight only.
        double totalWeight = 0;
        for (ShippingItem item : request.items()) {
            totalWeight += item.actualWeight() * item.quantity();
        }

        String province = request.deliveryProvince().toLowerCase(Locale.ROOT);
        boolean innerCity = province.contains("ha noi")
                || province.contains("hanoi")
                || province.contains("ho chi minh");

        long shippingFee = innerCity ? INNER_CITY_BASE_FEE : OTHER_PROVINCE_BASE_FEE;
        double baseWeight = innerCity ? INNER_CITY_BASE_WEIGHT : OTHER_PROVINCE_BASE_WEIGHT;
        if (totalWeight > baseWeight) {
            shippingFee += (long) Math.ceil((totalWeight - baseWeight) / EXTRA_WEIGHT_STEP) * EXTRA_STEP_FEE;
        }

        if (request.subtotalExVat() > FREE_SHIPPING_THRESHOLD) {
            shippingFee = Math.max(shippingFee - Math.min(shippingFee, MAX_FREE_SHIPPING_DISCOUNT), 0);
        }

        return new ShippingFeeResult(shippingFee, STRATEGY_CODE);
    }
}

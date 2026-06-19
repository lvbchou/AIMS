package com.aims.service.shipping;

import java.util.List;

public record ShippingFeeRequest(
        String deliveryProvince,
        long subtotalExVat,
        List<ShippingItem> items
) {
    public ShippingFeeRequest {
        items = items == null ? List.of() : List.copyOf(items);
    }
}

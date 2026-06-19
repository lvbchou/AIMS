package com.aims.service.shipping;

public record ShippingItem(
        Integer productId,
        int quantity,
        double actualWeight,
        String dimensions
) {
}

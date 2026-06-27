package com.aims.service.placeorder;

public record ValidatedDeliveryInfo(
        String recipientName,
        String phoneNumber,
        String email,
        String deliveryProvince,
        String detailAddress,
        String note
) {
}

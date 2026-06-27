package com.aims.dto.order;

import lombok.Data;

@Data
public class DeliveryInfoRequest {
    private String recipientName;
    private String phoneNumber;
    private String email;
    private String deliveryProvince;
    private String detailAddress;
    private String note;
    private String expectedDeliveryDate;
}

package com.aims.dto.order;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedDeliveryInfo {
    private String recipientName;
    private String phoneNumber;
    private String email;
    private String deliveryProvince;
    private String detailAddress;
    private String note;
}

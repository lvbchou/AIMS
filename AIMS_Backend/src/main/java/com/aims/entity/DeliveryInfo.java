package com.aims.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * DeliveryInfo - holds details for order delivery.
 */
@Getter
@Setter
public class DeliveryInfo {
    private String name;
    private String phone;
    private String address;
    private String province;
    private String instructions;
}

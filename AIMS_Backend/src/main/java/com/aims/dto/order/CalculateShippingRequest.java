package com.aims.dto.order;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CalculateShippingRequest {
    private String deliveryProvince;
    private List<CartItemRequest> items = new ArrayList<>();
}

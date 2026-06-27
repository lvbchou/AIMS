package com.aims.dto.order;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlaceOrderRequest {
    private List<CartItemRequest> items = new ArrayList<>();
}

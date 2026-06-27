package com.aims.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlaceOrderRequest {
    private List<CartItemRequest> items = new ArrayList<>();
}

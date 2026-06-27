package com.aims.dto.order;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateInvoiceRequest {
    private DeliveryInfoRequest deliveryInfo;
    private List<CartItemRequest> items = new ArrayList<>();
}

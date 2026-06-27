package com.aims.service.placeorder;

import com.aims.dto.order.InvoiceLineResponse;
import com.aims.entity.product.Product;
import com.aims.service.shipping.ShippingItem;

import java.util.List;

public record CartValidationResult(
        long subtotalExVat,
        List<Product> products,
        List<Integer> quantities,
        List<InvoiceLineResponse> invoiceItems,
        List<ShippingItem> shippingItems
) {
}

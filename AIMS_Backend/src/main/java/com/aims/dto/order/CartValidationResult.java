package com.aims.dto.order;

import com.aims.entity.product.Product;
import com.aims.service.shipping.ShippingItem;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartValidationResult {
    private long subtotalExVat;
    private List<Product> products;
    private List<Integer> quantities;
    private List<InvoiceLineResponse> invoiceItems;
    private List<ShippingItem> shippingItems;
}

package com.aims.service.placeorder;

import com.aims.dto.order.CartItemRequest;
import com.aims.dto.order.InvoiceLineResponse;
import com.aims.dto.order.StockAvailabilityIssue;
import com.aims.entity.product.Product;
import com.aims.exception.InsufficientStockException;
import com.aims.exception.InvalidOrderException;
import com.aims.repository.product.ProductRepository;
import com.aims.service.shipping.ShippingItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutCartService {
    private final ProductRepository productRepository;

    public CheckoutCartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public CartValidationResult validateAndBuildCartContext(List<CartItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Your cart is empty.");
        }

        long subtotalExVat = 0;
        List<Product> products = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        List<InvoiceLineResponse> invoiceItems = new ArrayList<>();
        List<ShippingItem> shippingItems = new ArrayList<>();
        List<StockAvailabilityIssue> affectedItems = new ArrayList<>();

        for (CartItemRequest item : items) {
            if (item == null || item.getProductId() == null || item.getQuantity() == null) {
                throw new InvalidOrderException("Cart item must include productId and quantity.");
            }
            if (item.getQuantity() <= 0) {
                throw new InvalidOrderException("Ordered quantity must be greater than zero.");
            }

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new InvalidOrderException(
                            "Product does not exist. ID: " + item.getProductId()));
            if (!"active".equalsIgnoreCase(product.getStatus())) {
                throw new InvalidOrderException("Product '" + product.getTitle() + "' is not available for sale.");
            }
            if (item.getQuantity() > product.getQuantityInStock()) {
                affectedItems.add(StockAvailabilityIssue.builder()
                        .productId(product.getProductId())
                        .title(product.getTitle())
                        .requestedQuantity(item.getQuantity())
                        .availableQuantity(product.getQuantityInStock())
                        .build());
                continue;
            }

            products.add(product);
            quantities.add(item.getQuantity());
            subtotalExVat += product.getSellingPrice() * item.getQuantity();
            shippingItems.add(new ShippingItem(
                    product.getProductId(),
                    item.getQuantity(),
                    product.getWeight(),
                    product.getDimensions()));
            invoiceItems.add(InvoiceLineResponse.builder()
                    .productId(product.getProductId())
                    .title(product.getTitle())
                    .category(product.getCategory())
                    .image(product.getImage())
                    .quantity(item.getQuantity())
                    .unitPriceExVat(product.getSellingPrice())
                    .amountExVat(product.getSellingPrice() * item.getQuantity())
                    .build());
        }

        if (!affectedItems.isEmpty()) {
            throw new InsufficientStockException(affectedItems);
        }

        return new CartValidationResult(subtotalExVat, products, quantities, invoiceItems, shippingItems);
    }
}

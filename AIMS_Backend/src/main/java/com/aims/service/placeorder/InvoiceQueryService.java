package com.aims.service.placeorder;

import com.aims.constants.OrderStatusValues;
import com.aims.dto.InvoiceLineItemDTO;
import com.aims.dto.InvoiceScreenDTO;
import com.aims.entity.Delivery;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.entity.OrderItem;
import com.aims.entity.product.Product;
import com.aims.exception.InvalidOrderException;
import com.aims.repository.DeliveryRepository;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderItemRepository;
import com.aims.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceQueryService {
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderItemRepository orderItemRepository;

    public InvoiceQueryService(
            OrderRepository orderRepository,
            InvoiceRepository invoiceRepository,
            DeliveryRepository deliveryRepository,
            OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.deliveryRepository = deliveryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public InvoiceScreenDTO getInvoiceScreen(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new InvalidOrderException("Order ID is required.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Order does not exist. ID: " + orderId));
        if (!OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus())) {
            throw new InvalidOrderException("Only pending payment orders can be viewed as an invoice.");
        }

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvalidOrderException("Invoice does not exist for order ID: " + orderId));
        Delivery delivery = deliveryRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Delivery information does not exist for order ID: " + orderId));

        List<OrderItem> orderItems = orderItemRepository.findAllWithProductByOrderId(orderId);
        List<InvoiceLineItemDTO> invoiceLines = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            invoiceLines.add(InvoiceLineItemDTO.builder()
                    .productId(product.getProductId())
                    .productTitle(product.getTitle())
                    .category(product.getCategory())
                    .image(product.getImage())
                    .quantity(orderItem.getQuantity())
                    .unitSellingPrice(product.getSellingPrice())
                    .lineTotalSellingPrice(product.getSellingPrice() * orderItem.getQuantity())
                    .build());
        }

        return InvoiceScreenDTO.builder()
                .orderId(orderId)
                .invoiceId(invoice.getInvoiceId())
                .issueDate(invoice.getIssueDate())
                .lineItems(invoiceLines)
                .totalProductPriceExclVat(invoice.getSubTotalExVAT())
                .totalProductPriceInclVat(invoice.getSubTotalIncVAT())
                .deliveryFee(invoice.getShippingFee())
                .totalAmountToBePaid(invoice.getSubTotalIncVAT() + invoice.getShippingFee())
                .recipientName(delivery.getRecipientName())
                .phoneNumber(delivery.getPhoneNumber())
                .email(delivery.getEmail())
                .detailAddress(delivery.getDetailAddress())
                .province(delivery.getDeliveryProvince())
                .build();
    }
}

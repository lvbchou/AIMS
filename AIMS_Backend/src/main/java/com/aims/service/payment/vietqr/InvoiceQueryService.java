package com.aims.service.payment.vietqr;

import org.springframework.stereotype.Service;

import com.aims.constants.OrderStatusValues;
import com.aims.dto.order.InvoiceScreenDTO;
import com.aims.dto.order.OrderConfirmationDTO;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import com.aims.exception.InvoiceNotFoundException;
import com.aims.exception.OrderNotFoundException;
import com.aims.exception.OrderNotPayableException;
import com.aims.repository.DeliveryRepository;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderItemRepository;
import com.aims.repository.OrderRepository;
import java.util.List;
import com.aims.dto.order.InvoiceLineItemDTO;
import lombok.RequiredArgsConstructor;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import com.aims.entity.Delivery;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.TransactionStatus;
import com.aims.exception.PaymentTransactionNotFoundException;
import com.aims.repository.PaymentTransactionRepository;

@Service("vietQrInvoiceQueryService")
@RequiredArgsConstructor
public class InvoiceQueryService {
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public InvoiceScreenDTO getInvoiceScreen(String orderId) {

        // 1. Fetch order and validate status
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!OrderStatusValues.AWAITING_PAYMENT.equals(order.getStatus())) {
            throw new OrderNotPayableException("Order " + orderId + " is not in AWAITING_PAYMENT state");
        }

        // 2. Fetch invoice
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotFoundException(orderId));

        // 3. Validate delivery existence (use existById to avoid loading full entity)
        if (!deliveryRepository.existsById(orderId)) {
            throw new OrderNotPayableException("Delivery " + orderId + " does not exist");
        }
        // 4. Calculate totals
        long totalEx = invoice.getSubTotalExVAT();
        long totalInc = invoice.getSubTotalIncVAT();
        long deliveryFee = invoice.getShippingFee();
        long totalToPay = totalInc + deliveryFee;

        // 5. Fetch and map OrderItems to DTOs using Java Stream
        List<com.aims.entity.OrderItem> items = orderItemRepository.findAllWithProductByOrderId(orderId);

        List<InvoiceLineItemDTO> lines = items.stream()
                .map(oi -> InvoiceLineItemDTO.builder()
                        .productTitle(oi.getProduct().getTitle())
                        .quantity(oi.getQuantity())
                        .unitSellingPrice(oi.getProduct().getSellingPrice())
                        .lineTotalSellingPrice(oi.getProduct().getSellingPrice() * oi.getQuantity())
                        .build())
                .toList();

        // 6. Build and return the final DTO directly
        return InvoiceScreenDTO.builder()
                .orderId(orderId)
                .invoiceId(invoice.getInvoiceId())
                .lineItems(lines)
                .totalProductPriceExclVat(totalEx)
                .totalProductPriceInclVat(totalInc)
                .deliveryFee(deliveryFee)
                .totalAmountToBePaid(totalToPay)
                .build();
    }

    public OrderConfirmationDTO getOrderConfirmation(String orderId) {
        // 1. Fetch order and validate
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. Fetch invoice
        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotFoundException(orderId));

        // 3. Fetch delivery information
        Delivery delivery = deliveryRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotPayableException("Delivery information missing"));

        // 4. Fetch the successful payment transaction for this invoice
        PaymentTransaction txn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), TransactionStatus.success)
                .orElseThrow(() -> new PaymentTransactionNotFoundException(
                        "Payment not yet confirmed for order " + orderId));

        // 5. Format the transaction time to DD/MM/YYYY HH:MM:SS
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ZonedDateTime zdt = txn.getTransactionTime().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        String dt = zdt.format(fmt);

        // 6. Build a human-readable order name from the order items
        List<com.aims.entity.OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
        String orderName;
        if (items.isEmpty()) {
            orderName = "Order #" + orderId.replace("-", "")
                    .substring(Math.max(0, orderId.replace("-", "").length() - 8)).toUpperCase();
        } else {
            String firstName = items.get(0).getProduct().getTitle();
            orderName = items.size() > 1
                    ? firstName + " and " + (items.size() - 1) + " other product(s)"
                    : firstName;
        }

        // 7. Calculate total amount to be paid
        long totalAmountToBePaid = invoice.getSubTotalIncVAT() + invoice.getShippingFee();

        // 8. Build and return the final confirmation DTO
        return OrderConfirmationDTO.builder()
                .customerName(delivery.getRecipientName())
                .phoneNumber(delivery.getPhoneNumber())
                .shippingAddress(delivery.getDetailAddress())
                .province(delivery.getDeliveryProvince())
                .totalAmountToBePaid(totalAmountToBePaid)
                .transactionId(txn.getTransactionId())
                .transactionContent(txn.getContent())
                .orderName(orderName)
                .transactionDatetimeDisplay(dt)
                .build();
    }
}

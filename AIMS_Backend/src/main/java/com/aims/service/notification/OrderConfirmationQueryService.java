package com.aims.service.notification;

import com.aims.dto.order.OrderConfirmationDTO;
import com.aims.entity.Delivery;
import com.aims.entity.Invoice;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.TransactionStatus;
import com.aims.repository.DeliveryRepository;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderItemRepository;
import com.aims.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class OrderConfirmationQueryService {
    private final DeliveryRepository deliveryRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public OrderConfirmationQueryService(
            DeliveryRepository deliveryRepository,
            InvoiceRepository invoiceRepository,
            OrderItemRepository orderItemRepository,
            PaymentTransactionRepository paymentTransactionRepository) {
        this.deliveryRepository = deliveryRepository;
        this.invoiceRepository = invoiceRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<NotificationRecipient> findRecipient(String orderId) {
        return deliveryRepository.findById(orderId)
                .map(delivery -> new NotificationRecipient(
                        delivery.getRecipientName(),
                        delivery.getEmail(),
                        delivery.getPhoneNumber(),
                        null,
                        null));
    }

    @Transactional(readOnly = true)
    public OrderConfirmationDTO findOrderConfirmation(String orderId) {
        Delivery delivery = deliveryRepository.findById(orderId)
                .orElseThrow(() -> new com.aims.exception.InvalidOrderException(
                        "Delivery information does not exist for order ID: " + orderId));

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new com.aims.exception.InvoiceNotFoundException(orderId));

        PaymentTransaction txn = paymentTransactionRepository
                .findFirstByInvoiceIdAndStatusInOrderByTransactionTimeDesc(
                        invoice.getInvoiceId(), List.of(TransactionStatus.success, TransactionStatus.refunded))
                .orElseThrow(() -> new com.aims.exception.PaymentTransactionNotFoundException(
                        "payment not yet confirmed for order " + orderId));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ZonedDateTime zdt = txn.getTransactionTime().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<com.aims.entity.OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
        String orderName;
        if (items.isEmpty()) {
            String compact = orderId.replace("-", "");
            orderName = "Order #" + compact.substring(Math.max(0, compact.length() - 8)).toUpperCase();
        } else {
            String firstName = items.get(0).getProduct().getTitle();
            orderName = items.size() > 1
                    ? firstName + " and " + (items.size() - 1) + " other products"
                    : firstName;
        }

        return OrderConfirmationDTO.builder()
                .customerName(delivery.getRecipientName())
                .phoneNumber(delivery.getPhoneNumber())
                .shippingAddress(delivery.getDetailAddress())
                .province(delivery.getDeliveryProvince())
                .totalAmountToBePaid(invoice.getSubTotalIncVAT() + invoice.getShippingFee())
                .transactionId(txn.getTransactionId())
                .transactionContent(txn.getContent())
                .orderName(orderName)
                .transactionDatetimeDisplay(zdt.format(fmt))
                .build();
    }
}

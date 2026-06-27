package com.aims.service.notification;

import com.aims.dto.order.OrderConfirmationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final OrderConfirmationQueryService orderConfirmationQueryService;
    private final OrderNotificationContentBuilder contentBuilder;
    private final NotificationSenderResolver senderResolver;
    private final List<NotificationChannel> orderSuccessChannels;

    public NotificationService(
            OrderConfirmationQueryService orderConfirmationQueryService,
            OrderNotificationContentBuilder contentBuilder,
            NotificationSenderResolver senderResolver,
            @Value("${aims.notification.order-success.channels:EMAIL}") String orderSuccessChannels) {
        this.orderConfirmationQueryService = orderConfirmationQueryService;
        this.contentBuilder = contentBuilder;
        this.senderResolver = senderResolver;
        this.orderSuccessChannels = parseChannels(orderSuccessChannels);
    }

    public void sendOrderSuccessNotification(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }

        Optional<NotificationRecipient> recipient = orderConfirmationQueryService.findRecipient(orderId);
        if (recipient.isEmpty()) {
            log.info("Skip order success notification because delivery contact is missing for orderId={}", orderId);
            return;
        }

        OrderConfirmationDTO confirmation = orderConfirmationQueryService.findOrderConfirmation(orderId);
        NotificationMessage message = contentBuilder.buildOrderSuccessMessage(orderId, confirmation);

        for (NotificationChannel channel : orderSuccessChannels) {
            try {
                senderResolver.resolve(channel).send(recipient.get(), message);
            } catch (Exception ex) {
                log.warn("Failed to send order success notification via {} for orderId={}: {}",
                        channel, orderId, ex.getMessage());
            }
        }
    }

    private List<NotificationChannel> parseChannels(String rawChannels) {
        if (rawChannels == null || rawChannels.isBlank()) {
            return List.of(NotificationChannel.EMAIL);
        }
        return Arrays.stream(rawChannels.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> NotificationChannel.valueOf(value.toUpperCase()))
                .toList();
    }
}

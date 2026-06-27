package com.aims.service;

import com.aims.service.notification.NotificationService;
import org.springframework.stereotype.Service;

/**
 * Backward-compatible facade for older callers.
 * New code should use NotificationService so Place Order is not coupled to email.
 */
@Service
public class OrderSuccessEmailService {
    private final NotificationService notificationService;

    public OrderSuccessEmailService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void sendOrderSuccessEmail(String orderId) {
        notificationService.sendOrderSuccessNotification(orderId);
    }
}

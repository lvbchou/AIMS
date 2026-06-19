package com.aims.service.notification;

public record NotificationMessage(
        String subject,
        String plainText,
        String html
) {
}

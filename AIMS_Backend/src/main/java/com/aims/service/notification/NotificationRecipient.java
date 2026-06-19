package com.aims.service.notification;

public record NotificationRecipient(
        String customerName,
        String email,
        String phoneNumber,
        String zaloUserId,
        String pushToken
) {
}

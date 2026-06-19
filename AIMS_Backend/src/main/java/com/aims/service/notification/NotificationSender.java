package com.aims.service.notification;

public interface NotificationSender {
    NotificationChannel getChannel();

    void send(NotificationRecipient recipient, NotificationMessage message);
}

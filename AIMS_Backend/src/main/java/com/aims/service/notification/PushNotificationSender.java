package com.aims.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationSender implements NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(PushNotificationSender.class);

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(NotificationRecipient recipient, NotificationMessage message) {
        log.info("Push notification sender is not connected to a push provider yet; skip pushToken={}",
                recipient == null ? null : recipient.pushToken());
    }
}

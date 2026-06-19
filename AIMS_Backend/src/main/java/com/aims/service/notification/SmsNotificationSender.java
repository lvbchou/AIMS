package com.aims.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationSender implements NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(NotificationRecipient recipient, NotificationMessage message) {
        log.info("SMS notification sender is not connected to an SMS gateway yet; skip phone={}",
                recipient == null ? null : recipient.phoneNumber());
    }
}

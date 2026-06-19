package com.aims.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZaloNotificationSender implements NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(ZaloNotificationSender.class);

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.ZALO;
    }

    @Override
    public void send(NotificationRecipient recipient, NotificationMessage message) {
        log.info("Zalo notification sender is not connected to Zalo API yet; skip zaloUserId={}",
                recipient == null ? null : recipient.zaloUserId());
    }
}

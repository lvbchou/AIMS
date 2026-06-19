package com.aims.service.notification;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationSenderResolver {
    private final Map<NotificationChannel, NotificationSender> senders = new EnumMap<>(NotificationChannel.class);

    public NotificationSenderResolver(List<NotificationSender> notificationSenders) {
        for (NotificationSender sender : notificationSenders) {
            senders.put(sender.getChannel(), sender);
        }
    }

    public NotificationSender resolve(NotificationChannel channel) {
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalStateException("No notification sender registered for channel: " + channel);
        }
        return sender;
    }
}

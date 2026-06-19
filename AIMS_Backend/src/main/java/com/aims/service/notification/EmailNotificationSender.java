package com.aims.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailNotificationSender(
            JavaMailSender mailSender,
            @Value("${aims.mail.from:no-reply@aims.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationRecipient recipient, NotificationMessage message) {
        if (recipient == null || recipient.email() == null || recipient.email().isBlank()) {
            log.info("Skip email notification because recipient email is missing.");
            return;
        }

        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(recipient.email().trim());
            helper.setSubject(message.subject());
            helper.setText(message.plainText(), message.html());
            mailSender.send(mimeMessage);
            log.info("Sent email notification to {}", recipient.email());
        } catch (Exception ex) {
            log.warn("Failed to send email notification to {}: {}", recipient.email(), ex.getMessage());
        }
    }
}

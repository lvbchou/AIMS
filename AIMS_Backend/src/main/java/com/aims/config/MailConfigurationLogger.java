package com.aims.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailConfigurationLogger {

    private static final Logger log = LoggerFactory.getLogger(MailConfigurationLogger.class);

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${spring.mail.port:}")
    private String port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${aims.mail.from:}")
    private String fromAddress;

    @PostConstruct
    public void logMailConfiguration() {
        log.info(
                "AIMS mail config resolved: host={}, port={}, username={}, from={}, passwordSet={}, passwordLength={}",
                blank(host),
                blank(port),
                blank(username),
                blank(fromAddress),
                password != null && !password.isBlank(),
                password == null ? 0 : password.length());

        if (password != null && password.contains(" ")) {
            log.warn("AIMS mail config warning: SMTP password contains spaces. Gmail App Password should usually be 16 characters without spaces in .env.");
        }
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? "<blank>" : value;
    }
}

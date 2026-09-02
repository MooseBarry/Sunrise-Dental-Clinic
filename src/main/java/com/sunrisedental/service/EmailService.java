package com.sunrisedental.service;

import com.sunrisedental.config.ApplicationConfig;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailService {
    private static final Logger LOGGER =
            Logger.getLogger(EmailService.class.getName());

    public boolean isEnabled() {
        return ApplicationConfig.booleanValue(
                "MAIL_ENABLED",
                "mail.enabled",
                false
        );
    }

    public boolean send(String recipient, String subject, String body) {
        if (!isEnabled() || recipient == null || recipient.isBlank()) {
            return false;
        }

        try {
            String username = ApplicationConfig.required(
                    "MAIL_USERNAME",
                    "mail.username"
            );
            String password = ApplicationConfig.required(
                    "MAIL_PASSWORD",
                    "mail.password"
            );
            String from = ApplicationConfig.optional(
                    "MAIL_FROM",
                    "mail.from",
                    username
            );

            Properties properties = new Properties();
            properties.put(
                    "mail.smtp.host",
                    ApplicationConfig.required("MAIL_HOST", "mail.host")
            );
            properties.put("mail.smtp.port", Integer.toString(
                    ApplicationConfig.integerValue(
                            "MAIL_PORT", "mail.port", 587
                    )
            ));
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", Boolean.toString(
                    ApplicationConfig.booleanValue(
                            "MAIL_STARTTLS", "mail.starttls", true
                    )
            ));
            properties.put("mail.smtp.connectiontimeout", "10000");
            properties.put("mail.smtp.timeout", "10000");

            Session session = Session.getInstance(
                    properties,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication
                        getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    username,
                                    password
                            );
                        }
                    }
            );

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(recipient)
            );
            message.setSubject(
                    "Sunrise Dental Clinic | " + subject,
                    StandardCharsets.UTF_8.name()
            );
            message.setText(
                    body,
                    StandardCharsets.UTF_8.name()
            );
            Transport.send(message);
            return true;
        } catch (MessagingException | IllegalStateException exception) {
            LOGGER.log(Level.WARNING,
                    "Patient email notification could not be sent.",
                    exception);
            return false;
        }
    }
}

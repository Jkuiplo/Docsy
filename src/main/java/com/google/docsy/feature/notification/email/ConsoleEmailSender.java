package com.google.docsy.feature.notification.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "application.mail.sender", havingValue = "console", matchIfMissing = true)
public class ConsoleEmailSender implements EmailSender {

    @Override
    public void sendEmail(String to, String subject, String text) {
        System.out.println("\n==========================================================");
        System.out.println("📧 MOCK EMAIL SENT");
        System.out.println("To:      " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + text);
        System.out.println("==========================================================\n");
    }
}
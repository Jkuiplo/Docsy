package com.google.docsy.feature.notification.email;

public interface EmailSender {
    void sendEmail(String to, String subject, String text);
}
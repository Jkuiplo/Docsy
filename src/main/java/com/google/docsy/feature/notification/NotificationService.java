package com.google.docsy.feature.notification;

import com.google.docsy.enums.NotificationStatus;
import com.google.docsy.enums.NotificationType;
import com.google.docsy.feature.notification.email.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;

    // The base frontend URL for links
    private final String baseUrl = "http://localhost:3000";

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify your Docsy account";
        String link = baseUrl + "/verify-email?token=" + token;
        String text = "Welcome to Docsy!\n\nPlease click the link below to verify your email address:\n" + link;
        
        sendAndLog(toEmail, subject, text, NotificationType.EMAIL_VERIFICATION);
    }

    @Async
    public void sendInvitationEmail(String toEmail, String workspaceName, String token) {
        String subject = "You've been invited to join " + workspaceName;
        String link = baseUrl + "/join?token=" + token;
        String text = "You have been invited to collaborate in " + workspaceName + " on Docsy.\n\nClick here to accept the invitation:\n" + link;

        sendAndLog(toEmail, subject, text, NotificationType.INVITATION);
    }

    @Async
    public void sendDocumentStatusEmail(String toEmail, String documentTitle, String newStatus, String comment) {
        String subject = "Document Update: " + documentTitle;
        String text = "The document '" + documentTitle + "' has been moved to status: " + newStatus + ".";
        
        if (comment != null && !comment.isBlank()) {
            text += "\n\nNote/Reason:\n" + comment;
        }

        sendAndLog(toEmail, subject, text, NotificationType.DOCUMENT_STATUS);
    }

    private void sendAndLog(String to, String subject, String text, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipientEmail(to);
        notification.setSubject(subject);
        notification.setMessage(text);
        notification.setType(type);
        notification.setStatus(NotificationStatus.PENDING);

        notification = notificationRepository.save(notification);

        try {
            emailSender.sendEmail(to, subject, text);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        } finally {
            notificationRepository.save(notification);
        }
    }
}
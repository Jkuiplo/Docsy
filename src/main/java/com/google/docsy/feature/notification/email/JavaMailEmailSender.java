// package com.google.docsy.feature.notification.email;

// import lombok.RequiredArgsConstructor;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Component;

// @Component
// @ConditionalOnProperty(name = "application.mail.sender", havingValue = "java-mail")
// @RequiredArgsConstructor
// public class JavaMailEmailSender implements EmailSender {

//     private final JavaMailSender mailSender;

//     @Override
//     public void sendEmail(String to, String subject, String text) {
//         SimpleMailMessage message = new SimpleMailMessage();
//         message.setTo(to);
//         message.setSubject(subject);
//         message.setText(text);
//         // message.setFrom("noreply@docsy.com"); // Configure in properties
        
//         mailSender.send(message);
//     }
// }
package com.google.docsy.feature.emailVerification;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public String createVerificationToken(User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusHours(24)); // Token valid for 24 hours
        tokenRepository.save(token);

        // Vibe coding debug: Print to terminal so we can test in Postman before MailSender is ready
        System.out.println("--------------------------------------------------");
        System.out.println("VERIFICATION TOKEN FOR " + user.getEmail() + ":");
        System.out.println(token.getToken());
        System.out.println("--------------------------------------------------");

        return token.getToken();
    }

    @Transactional
    public void verifyEmail(String tokenStr) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (token.getUsedAt() != null) {
            throw new BadRequestException("Token has already been used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        // Mark token as used
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        // Update user status
        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
    }
}
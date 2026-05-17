package com.google.docsy.feature.user;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.feature.user.dto.request.ChangePasswordRequest;
import com.google.docsy.feature.user.dto.request.UpdateProfileRequest;
import com.google.docsy.feature.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {
        
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            currentUser.setFullName(request.getFullName());
        }
        
        // Position title is allowed to be cleared out, so we don't check for isBlank()
        if (request.getPositionTitle() != null) {
            currentUser.setPositionTitle(request.getPositionTitle());
        }

        User updatedUser = userRepository.save(currentUser);
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters long");
        }

        currentUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .positionTitle(user.getPositionTitle())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
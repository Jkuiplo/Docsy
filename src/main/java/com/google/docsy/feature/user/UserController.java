package com.google.docsy.feature.user;

import com.google.docsy.common.security.CurrentUserProvider;
import com.google.docsy.feature.user.dto.request.ChangePasswordRequest;
import com.google.docsy.feature.user.dto.request.UpdateProfileRequest;
import com.google.docsy.feature.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User currentUser = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(userService.mapToResponse(currentUser));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(userService.updateProfile(currentUser, request));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        userService.changePassword(currentUser, request);
        return ResponseEntity.ok().build();
    }
}
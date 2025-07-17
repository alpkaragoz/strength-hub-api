package com.strengthhub.strength_hub_api.controller;

import com.strengthhub.strength_hub_api.dto.request.auth.LoginRequest;
import com.strengthhub.strength_hub_api.dto.request.auth.RefreshTokenRequest;
import com.strengthhub.strength_hub_api.dto.request.user.UserRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.response.auth.JwtAuthenticationResponse;
import com.strengthhub.strength_hub_api.dto.response.auth.TokenRefreshResponse;
import com.strengthhub.strength_hub_api.dto.response.user.UserResponse;
import com.strengthhub.strength_hub_api.security.SecurityUtils;
import com.strengthhub.strength_hub_api.service.AuthService;
import com.strengthhub.strength_hub_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtAuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all-devices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logoutFromAllDevices() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        authService.logoutFromAllDevices(currentUserId);
        return ResponseEntity.noContent().build();
    }
}
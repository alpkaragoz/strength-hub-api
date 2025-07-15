package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.request.auth.LoginRequest;
import com.strengthhub.strength_hub_api.dto.request.auth.RefreshTokenRequest;
import com.strengthhub.strength_hub_api.dto.response.auth.JwtAuthenticationResponse;
import com.strengthhub.strength_hub_api.dto.response.auth.TokenRefreshResponse;
import com.strengthhub.strength_hub_api.exception.auth.TokenRefreshException;
import com.strengthhub.strength_hub_api.model.RefreshToken;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.RefreshTokenRepository;
import com.strengthhub.strength_hub_api.repository.UserRepository;
import com.strengthhub.strength_hub_api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${app.jwt.sliding-refresh-days}")
    private int slidingRefreshDays;

    @Value("${app.jwt.max-refresh-tokens-per-user}")
    private int maxRefreshTokensPerUser;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public JwtAuthenticationResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());

        // Find user by username or email
        User user = findUserByUsernameOrEmail(request.getUsernameOrEmail());

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", request.getUsernameOrEmail());
            throw new BadCredentialsException("Invalid username/email or password");
        }

        // Generate new tokens with token limit enforcement
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Save refresh token with limit enforcement
        saveRefreshTokenWithLimit(user, refreshToken);

        // Build roles set for response
        Set<com.strengthhub.strength_hub_api.enums.UserType> roles = buildUserRoles(user);

        log.info("User {} logged in successfully", user.getUsername());

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationTime())
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isAdmin(user.getIsAdmin())
                .roles(roles)
                .build();
    }

    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        String requestRefreshToken = request.getRefreshToken();

        // Validate refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            log.warn("Invalid or expired refresh token");
            throw new TokenRefreshException("Refresh token is expired or revoked");
        }

        // Generate new access token and refresh token
        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        // Save refresh token with limit enforcement
        saveRefreshTokenWithLimit(user, newRefreshToken);

        log.info("Token refreshed successfully for user: {}",
                user.getUsername());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationTime())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        log.info("Logout attempt with refresh token");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new TokenRefreshException("Refresh token is required for logout");
        }

        // Find and revoke the specific refresh token
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        // Mark this specific token as revoked
        refreshTokenRepository.revokeTokenByValue(refreshToken);

        log.info("User {} logged out successfully from device", token.getUser().getUsername());
    }

    @Transactional
    public void logoutFromAllDevices(UUID userId) {
        log.info("Logout from all devices for user: {}", userId);

        // Revoke all refresh tokens for this user (emergency use)
        refreshTokenRepository.revokeAllUserTokens(userId);

        log.info("User {} logged out from all devices", userId);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired and revoked refresh tokens");
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
    }

    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        // Check if it looks like an email
        if (usernameOrEmail.contains("@")) {
            return userRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));
        } else {
            return userRepository.findByUsername(usernameOrEmail)
                    .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));
        }
    }

    @Transactional
    private void saveRefreshTokenWithLimit(User user, String tokenValue) {
        // Calculate expiration time (sliding window starts from now)
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(slidingRefreshDays);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(expirationTime)
                .isRevoked(false)
                .build();

        long userTokenCount = getActiveTokenCountForUser(user.getUserId());
        if(userTokenCount > maxRefreshTokensPerUser) {
            refreshTokenRepository.deleteOldestTokenForUser(user.getUserId());
        }

        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token saved for user: {} (expires: {})", user.getUsername(), expirationTime);
    }

    @Transactional(readOnly = true)
    private long getActiveTokenCountForUser(UUID userId) {
        return refreshTokenRepository.countActiveTokensByUserId(userId, LocalDateTime.now());
    }

    private Set<com.strengthhub.strength_hub_api.enums.UserType> buildUserRoles(User user) {
        Set<com.strengthhub.strength_hub_api.enums.UserType> roles = new HashSet<>();
        if (user.isLifter()) roles.add(com.strengthhub.strength_hub_api.enums.UserType.LIFTER);
        if (user.isCoach()) roles.add(com.strengthhub.strength_hub_api.enums.UserType.COACH);
        return roles;
    }
}
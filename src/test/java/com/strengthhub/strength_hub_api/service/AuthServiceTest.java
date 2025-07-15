package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.request.auth.LoginRequest;
import com.strengthhub.strength_hub_api.dto.request.auth.RefreshTokenRequest;
import com.strengthhub.strength_hub_api.dto.response.auth.JwtAuthenticationResponse;
import com.strengthhub.strength_hub_api.dto.response.auth.TokenRefreshResponse;
import com.strengthhub.strength_hub_api.enums.UserType;
import com.strengthhub.strength_hub_api.exception.auth.TokenRefreshException;
import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.Lifter;
import com.strengthhub.strength_hub_api.model.RefreshToken;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.RefreshTokenRepository;
import com.strengthhub.strength_hub_api.repository.UserRepository;
import com.strengthhub.strength_hub_api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RefreshToken testRefreshToken;
    private LoginRequest validLoginRequest;
    private RefreshTokenRequest validRefreshTokenRequest;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        // Set test configuration values
        ReflectionTestUtils.setField(authService, "slidingRefreshDays", 30);
        ReflectionTestUtils.setField(authService, "maxRefreshTokensPerUser", 5);

        testUser = User.builder()
                .userId(testUserId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encoded_password")
                .firstName("Test")
                .lastName("User")
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Add lifter profile
        Lifter lifter = Lifter.builder()
                .lifterId(testUserId)
                .app_user(testUser)
                .build();
        testUser.setLifterProfile(lifter);

        testRefreshToken = RefreshToken.builder()
                .tokenId(UUID.randomUUID())
                .token("valid_refresh_token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isRevoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        validLoginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        validRefreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("valid_refresh_token")
                .build();
    }

    @Test
    @DisplayName("Should login successfully with valid username and password")
    void login_WithValidUsernameAndPassword_ShouldReturnJwtResponse() {
        // Given
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", "encoded_password")).willReturn(true);
        given(jwtUtil.generateAccessToken(testUser)).willReturn("access_token");
        given(jwtUtil.generateRefreshToken(testUser)).willReturn("refresh_token");
        given(jwtUtil.getAccessTokenExpirationTime()).willReturn(86400000L);
        given(refreshTokenRepository.countActiveTokensByUserId(any(UUID.class), any(LocalDateTime.class))).willReturn(0L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(testRefreshToken);

        // When
        JwtAuthenticationResponse result = authService.login(validLoginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access_token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getIsAdmin()).isFalse();
        assertThat(result.getRoles()).containsExactly(UserType.LIFTER);

        then(refreshTokenRepository).should().save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should login successfully with valid email and password")
    void login_WithValidEmailAndPassword_ShouldReturnJwtResponse() {
        // Given
        LoginRequest emailLoginRequest = LoginRequest.builder()
                .usernameOrEmail("test@example.com")
                .password("password123")
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", "encoded_password")).willReturn(true);
        given(jwtUtil.generateAccessToken(testUser)).willReturn("access_token");
        given(jwtUtil.generateRefreshToken(testUser)).willReturn("refresh_token");
        given(jwtUtil.getAccessTokenExpirationTime()).willReturn(86400000L);
        given(refreshTokenRepository.countActiveTokensByUserId(any(UUID.class), any(LocalDateTime.class))).willReturn(0L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(testRefreshToken);

        // When
        JwtAuthenticationResponse result = authService.login(emailLoginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        then(userRepository).should().findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found by username")
    void login_WithInvalidUsername_ShouldThrowBadCredentialsException() {
        // Given
        given(userRepository.findByUsername("invaliduser")).willReturn(Optional.empty());

        LoginRequest invalidRequest = LoginRequest.builder()
                .usernameOrEmail("invaliduser")
                .password("password123")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.login(invalidRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username/email or password");
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void login_WithInvalidPassword_ShouldThrowBadCredentialsException() {
        // Given
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("wrongpassword", "encoded_password")).willReturn(false);

        LoginRequest invalidRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("wrongpassword")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.login(invalidRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username/email or password");
    }

    @Test
    @DisplayName("Should include both roles for user with both lifter and coach profiles")
    void login_WithUserHavingBothRoles_ShouldIncludeBothRoles() {
        // Given
        Coach coach = Coach.builder()
                .coachId(testUserId)
                .app_user(testUser)
                .build();
        testUser.setCoachProfile(coach);

        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", "encoded_password")).willReturn(true);
        given(jwtUtil.generateAccessToken(testUser)).willReturn("access_token");
        given(jwtUtil.generateRefreshToken(testUser)).willReturn("refresh_token");
        given(jwtUtil.getAccessTokenExpirationTime()).willReturn(86400000L);
        given(refreshTokenRepository.countActiveTokensByUserId(any(UUID.class), any(LocalDateTime.class))).willReturn(0L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(testRefreshToken);

        // When
        JwtAuthenticationResponse result = authService.login(validLoginRequest);

        // Then
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserType.LIFTER, UserType.COACH);
    }

    @Test
    @DisplayName("Should refresh token successfully with valid refresh token")
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        // Given
        given(refreshTokenRepository.findByToken("valid_refresh_token")).willReturn(Optional.of(testRefreshToken));
        given(jwtUtil.generateAccessToken(testUser)).willReturn("new_access_token");
        given(jwtUtil.generateRefreshToken(testUser)).willReturn("new_refresh_token");
        given(jwtUtil.getAccessTokenExpirationTime()).willReturn(86400000L);
        given(refreshTokenRepository.countActiveTokensByUserId(any(UUID.class), any(LocalDateTime.class))).willReturn(0L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(testRefreshToken);

        // When
        TokenRefreshResponse result = authService.refreshToken(validRefreshTokenRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new_access_token");
        assertThat(result.getRefreshToken()).isEqualTo("new_refresh_token");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isEqualTo(86400000L);

        then(refreshTokenRepository).should().save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception when refresh token not found")
    void refreshToken_WithInvalidToken_ShouldThrowTokenRefreshException() {
        // Given
        given(refreshTokenRepository.findByToken("invalid_token")).willReturn(Optional.empty());

        RefreshTokenRequest invalidRequest = RefreshTokenRequest.builder()
                .refreshToken("invalid_token")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(invalidRequest))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Refresh token not found");
    }

    @Test
    @DisplayName("Should throw exception when refresh token is expired")
    void refreshToken_WithExpiredToken_ShouldThrowTokenRefreshException() {
        // Given
        RefreshToken expiredToken = RefreshToken.builder()
                .tokenId(UUID.randomUUID())
                .token("expired_token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusDays(1)) // Expired
                .isRevoked(false)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        given(refreshTokenRepository.findByToken("expired_token")).willReturn(Optional.of(expiredToken));

        RefreshTokenRequest expiredRequest = RefreshTokenRequest.builder()
                .refreshToken("expired_token")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(expiredRequest))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("expired or revoked");
    }

    @Test
    @DisplayName("Should throw exception when refresh token is revoked")
    void refreshToken_WithRevokedToken_ShouldThrowTokenRefreshException() {
        // Given
        RefreshToken revokedToken = RefreshToken.builder()
                .tokenId(UUID.randomUUID())
                .token("revoked_token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isRevoked(true) // Revoked
                .createdAt(LocalDateTime.now())
                .build();

        given(refreshTokenRepository.findByToken("revoked_token")).willReturn(Optional.of(revokedToken));

        RefreshTokenRequest revokedRequest = RefreshTokenRequest.builder()
                .refreshToken("revoked_token")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(revokedRequest))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("expired or revoked");
    }

    @Test
    @DisplayName("Should logout successfully with valid refresh token")
    void logout_WithValidToken_ShouldRevokeToken() {
        // Given
        given(refreshTokenRepository.findByToken("valid_refresh_token")).willReturn(Optional.of(testRefreshToken));

        // When
        authService.logout("valid_refresh_token");

        // Then
        then(refreshTokenRepository).should().revokeTokenByValue("valid_refresh_token");
    }

    @Test
    @DisplayName("Should throw exception when logout with null token")
    void logout_WithNullToken_ShouldThrowTokenRefreshException() {
        // When & Then
        assertThatThrownBy(() -> authService.logout(null))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("required for logout");
    }

    @Test
    @DisplayName("Should throw exception when logout with empty token")
    void logout_WithEmptyToken_ShouldThrowTokenRefreshException() {
        // When & Then
        assertThatThrownBy(() -> authService.logout(""))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("required for logout");
    }

    @Test
    @DisplayName("Should throw exception when logout token not found")
    void logout_WithInvalidToken_ShouldThrowTokenRefreshException() {
        // Given
        given(refreshTokenRepository.findByToken("invalid_token")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.logout("invalid_token"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Refresh token not found");
    }

    @Test
    @DisplayName("Should logout from all devices successfully")
    void logoutFromAllDevices_WithValidUserId_ShouldRevokeAllTokens() {
        // When
        authService.logoutFromAllDevices(testUserId);

        // Then
        then(refreshTokenRepository).should().revokeAllUserTokens(testUserId);
    }

    @Test
    @DisplayName("Should cleanup expired tokens successfully")
    void cleanupExpiredTokens_ShouldDeleteExpiredAndRevokedTokens() {
        // When
        authService.cleanupExpiredTokens();

        // Then
        then(refreshTokenRepository).should().deleteExpiredAndRevokedTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should enforce token limit when saving refresh token")
    void login_WhenUserExceedsTokenLimit_ShouldDeleteOldestToken() {
        // Given
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", "encoded_password")).willReturn(true);
        given(jwtUtil.generateAccessToken(testUser)).willReturn("access_token");
        given(jwtUtil.generateRefreshToken(testUser)).willReturn("refresh_token");
        given(jwtUtil.getAccessTokenExpirationTime()).willReturn(86400000L);
        given(refreshTokenRepository.countActiveTokensByUserId(any(UUID.class), any(LocalDateTime.class))).willReturn(6L); // Exceeds limit of 5
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(testRefreshToken);

        // When
        authService.login(validLoginRequest);

        // Then
        then(refreshTokenRepository).should().deleteOldestTokenForUser(testUserId);
        then(refreshTokenRepository).should().save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should handle whitespace in token for logout")
    void logout_WithWhitespaceToken_ShouldThrowTokenRefreshException() {
        // When & Then
        assertThatThrownBy(() -> authService.logout("   "))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("required for logout");
    }

    @Test
    @DisplayName("Should correctly identify email vs username in login")
    void login_ShouldCorrectlyIdentifyEmailVsUsername() {
        // Given - Test with email format
        LoginRequest emailRequest = LoginRequest.builder()
                .usernameOrEmail("user@domain.com")
                .password("password123")
                .build();

        given(userRepository.findByEmail("user@domain.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", "encoded_password")).willReturn(true);
        given(jwtUtil.generateAccessToken(testUser)).willReturn("access_token");
        given(jwtUtil.generateRefreshToken(testUser)).willReturn("refresh_token");
        given(jwtUtil.getAccessTokenExpirationTime()).willReturn(86400000L);
        given(refreshTokenRepository.countActiveTokensByUserId(any(UUID.class), any(LocalDateTime.class))).willReturn(0L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(testRefreshToken);

        // When
        authService.login(emailRequest);

        // Then
        then(userRepository).should().findByEmail("user@domain.com");
        then(userRepository).should(times(0)).findByUsername(anyString());
    }
}
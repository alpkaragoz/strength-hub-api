package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.response.coach.CoachCodeResponse;
import com.strengthhub.strength_hub_api.enums.CoachCodeStatus;
import com.strengthhub.strength_hub_api.exception.common.UnauthorizedAccessException;
import com.strengthhub.strength_hub_api.exception.coach.InvalidCoachCodeException;
import com.strengthhub.strength_hub_api.model.CoachCode;
import com.strengthhub.strength_hub_api.repository.CoachCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoachCodeService Tests")
class CoachCodeServiceTest {

    @Mock
    private CoachCodeRepository coachCodeRepository;

    @InjectMocks
    private CoachCodeService coachCodeService;

    private String validSecretKey;
    private String invalidSecretKey;
    private CoachCode testCoachCode;
    private UUID testUserId;
    private UUID testCodeId;

    @BeforeEach
    void setUp() {
        validSecretKey = "correct-secret-key";
        invalidSecretKey = "wrong-secret-key";
        testUserId = UUID.randomUUID();
        testCodeId = UUID.randomUUID();

        // Set the secret key using reflection
        ReflectionTestUtils.setField(coachCodeService, "coachSecretKey", validSecretKey);

        testCoachCode = CoachCode.builder()
                .codeId(testCodeId)
                .code("TESTCODE")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isUsed(false)
                .usedBy(null)
                .usedAt(null)
                .build();
    }

    @Test
    @DisplayName("Should generate coach code successfully with valid secret key")
    void generateCoachCode_WithValidSecretKey_ShouldGenerateCode() {
        // Given
        given(coachCodeRepository.existsByCode(anyString())).willReturn(false);
        given(coachCodeRepository.save(any(CoachCode.class))).willReturn(testCoachCode);

        // When
        CoachCodeResponse result = coachCodeService.generateCoachCode();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCodeId()).isEqualTo(testCodeId);
        assertThat(result.getCode()).isEqualTo("TESTCODE");
        assertThat(result.getIsUsed()).isFalse();
        assertThat(result.getUsedBy()).isNull();
        assertThat(result.getUsedAt()).isNull();
        assertThat(result.getStatus()).isEqualTo(CoachCodeStatus.ACTIVE);
        assertThat(result.getExpiresAt()).isAfter(LocalDateTime.now());

        then(coachCodeRepository).should().save(any(CoachCode.class));
    }

    @Test
    @DisplayName("Should generate unique code when collision occurs")
    void generateCoachCode_WithCodeCollision_ShouldGenerateUniqueCode() {
        // Given
        given(coachCodeRepository.existsByCode(anyString()))
                .willReturn(true)  // First attempt collides
                .willReturn(false); // Second attempt is unique
        given(coachCodeRepository.save(any(CoachCode.class))).willReturn(testCoachCode);

        // When
        CoachCodeResponse result = coachCodeService.generateCoachCode();

        // Then
        assertThat(result).isNotNull();
        then(coachCodeRepository).should().save(any(CoachCode.class));
    }

    @Test
    @DisplayName("Should validate and use coach code successfully")
    void validateAndUseCoachCode_WithValidCode_ShouldMarkAsUsed() {
        // Given
        CoachCode savedCode = CoachCode.builder()
                .codeId(testCodeId)
                .code("VALIDCODE")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isUsed(false)
                .usedBy(null)
                .usedAt(null)
                .build();

        CoachCode usedCode = CoachCode.builder()
                .codeId(testCodeId)
                .code("VALIDCODE")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isUsed(true)
                .usedBy(testUserId)
                .usedAt(LocalDateTime.now())
                .build();

        given(coachCodeRepository.findByCode("VALIDCODE")).willReturn(Optional.of(savedCode));
        given(coachCodeRepository.save(any(CoachCode.class))).willReturn(usedCode);

        // When
        coachCodeService.validateAndUseCoachCode("VALIDCODE", testUserId);

        // Then
        assertThat(savedCode.getIsUsed()).isTrue();
        assertThat(savedCode.getUsedBy()).isEqualTo(testUserId);
        assertThat(savedCode.getUsedAt()).isNotNull();
        then(coachCodeRepository).should().save(savedCode);
    }

    @Test
    @DisplayName("Should throw exception when coach code not found")
    void validateAndUseCoachCode_WithInvalidCode_ShouldThrowInvalidCoachCodeException() {
        // Given
        given(coachCodeRepository.findByCode("INVALIDCODE")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> coachCodeService.validateAndUseCoachCode("INVALIDCODE", testUserId))
                .isInstanceOf(InvalidCoachCodeException.class)
                .hasMessageContaining("Code not found");

        then(coachCodeRepository).should(never()).save(any(CoachCode.class));
    }

    @Test
    @DisplayName("Should throw exception when coach code already used")
    void validateAndUseCoachCode_WithUsedCode_ShouldThrowInvalidCoachCodeException() {
        // Given
        CoachCode usedCode = CoachCode.builder()
                .codeId(testCodeId)
                .code("USEDCODE")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isUsed(true)
                .usedBy(UUID.randomUUID())
                .usedAt(LocalDateTime.now().minusHours(1))
                .build();

        given(coachCodeRepository.findByCode("USEDCODE")).willReturn(Optional.of(usedCode));

        // When & Then
        assertThatThrownBy(() -> coachCodeService.validateAndUseCoachCode("USEDCODE", testUserId))
                .isInstanceOf(InvalidCoachCodeException.class)
                .hasMessageContaining("already been used");

        then(coachCodeRepository).should(never()).save(any(CoachCode.class));
    }

    @Test
    @DisplayName("Should throw exception when coach code expired")
    void validateAndUseCoachCode_WithExpiredCode_ShouldThrowInvalidCoachCodeException() {
        // Given
        CoachCode expiredCode = CoachCode.builder()
                .codeId(testCodeId)
                .code("EXPIREDCODE")
                .createdAt(LocalDateTime.now().minusDays(2))
                .expiresAt(LocalDateTime.now().minusHours(1)) // Expired
                .isUsed(false)
                .usedBy(null)
                .usedAt(null)
                .build();

        given(coachCodeRepository.findByCode("EXPIREDCODE")).willReturn(Optional.of(expiredCode));

        // When & Then
        assertThatThrownBy(() -> coachCodeService.validateAndUseCoachCode("EXPIREDCODE", testUserId))
                .isInstanceOf(InvalidCoachCodeException.class)
                .hasMessageContaining("expired");

        then(coachCodeRepository).should(never()).save(any(CoachCode.class));
    }

    @Test
    @DisplayName("Should map to response with ACTIVE status for valid unused code")
    void mapToResponse_WithActiveCode_ShouldReturnActiveStatus() {
        // Given
        given(coachCodeRepository.existsByCode(anyString())).willReturn(false);
        given(coachCodeRepository.save(any(CoachCode.class))).willReturn(testCoachCode);

        // When
        CoachCodeResponse result = coachCodeService.generateCoachCode();

        // Then
        assertThat(result.getStatus()).isEqualTo(CoachCodeStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should map to response with USED status for used code")
    void mapToResponse_WithUsedCode_ShouldReturnUsedStatus() {
        // Given
        CoachCode usedCode = CoachCode.builder()
                .codeId(testCodeId)
                .code("USEDCODE")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isUsed(true)
                .usedBy(testUserId)
                .usedAt(LocalDateTime.now())
                .build();

        given(coachCodeRepository.existsByCode(anyString())).willReturn(false);
        given(coachCodeRepository.save(any(CoachCode.class))).willReturn(usedCode);

        // When
        CoachCodeResponse result = coachCodeService.generateCoachCode();

        // Then
        assertThat(result.getStatus()).isEqualTo(CoachCodeStatus.USED);
        assertThat(result.getUsedBy()).isEqualTo(testUserId);
        assertThat(result.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should map to response with EXPIRED status for expired code")
    void mapToResponse_WithExpiredCode_ShouldReturnExpiredStatus() {
        // Given
        CoachCode expiredCode = CoachCode.builder()
                .codeId(testCodeId)
                .code("EXPIREDCODE")
                .createdAt(LocalDateTime.now().minusDays(2))
                .expiresAt(LocalDateTime.now().minusHours(1)) // Expired
                .isUsed(false)
                .usedBy(null)
                .usedAt(null)
                .build();

        given(coachCodeRepository.existsByCode(anyString())).willReturn(false);
        given(coachCodeRepository.save(any(CoachCode.class))).willReturn(expiredCode);

        // When
        CoachCodeResponse result = coachCodeService.generateCoachCode();

        // Then
        assertThat(result.getStatus()).isEqualTo(CoachCodeStatus.EXPIRED);
    }

    @Test
    @DisplayName("Should generate code with proper expiration time")
    void generateCoachCode_ShouldSetProperExpirationTime() {
        // Given
        LocalDateTime beforeGeneration = LocalDateTime.now();
        given(coachCodeRepository.existsByCode(anyString())).willReturn(false);
        given(coachCodeRepository.save(any(CoachCode.class))).willAnswer(invocation -> {
            CoachCode savedCode = invocation.getArgument(0);
            // Verify expiration is set to 24 hours from creation
            assertThat(savedCode.getExpiresAt()).isAfter(beforeGeneration.plusHours(23));
            assertThat(savedCode.getExpiresAt()).isBefore(beforeGeneration.plusHours(25));
            return savedCode;
        });

        // When
        coachCodeService.generateCoachCode();

        // Then - assertions are in the answer above
        then(coachCodeRepository).should().save(any(CoachCode.class));
    }

    @Test
    @DisplayName("Should preserve original creation time when marking as used")
    void validateAndUseCoachCode_ShouldPreserveCreationTime() {
        // Given
        LocalDateTime originalCreationTime = LocalDateTime.now().minusHours(2);
        LocalDateTime originalExpirationTime = LocalDateTime.now().plusHours(22);

        CoachCode originalCode = CoachCode.builder()
                .codeId(testCodeId)
                .code("PRESERVECODE")
                .createdAt(originalCreationTime)
                .expiresAt(originalExpirationTime)
                .isUsed(false)
                .usedBy(null)
                .usedAt(null)
                .build();

        given(coachCodeRepository.findByCode("PRESERVECODE")).willReturn(Optional.of(originalCode));
        given(coachCodeRepository.save(any(CoachCode.class))).willAnswer(invocation -> {
            CoachCode savedCode = invocation.getArgument(0);
            // Verify original timestamps are preserved
            assertThat(savedCode.getCreatedAt()).isEqualTo(originalCreationTime);
            assertThat(savedCode.getExpiresAt()).isEqualTo(originalExpirationTime);
            return savedCode;
        });

        // When
        coachCodeService.validateAndUseCoachCode("PRESERVECODE", testUserId);

        // Then - assertions are in the answer above
        then(coachCodeRepository).should().save(originalCode);
    }

    @Test
    @DisplayName("Should validate twice used code throws exception")
    void validateAndUseCoachCode_CalledTwice_ShouldThrowExceptionOnSecondCall() {
        // Given
        CoachCode code = CoachCode.builder()
                .codeId(testCodeId)
                .code("ONCEONLY")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isUsed(false)
                .usedBy(null)
                .usedAt(null)
                .build();

        given(coachCodeRepository.findByCode("ONCEONLY")).willReturn(Optional.of(code));

        // First call
        coachCodeService.validateAndUseCoachCode("ONCEONLY", testUserId);

        // Code is now marked as used
        code.setIsUsed(true);
        code.setUsedBy(testUserId);
        code.setUsedAt(LocalDateTime.now());

        // When & Then - Second call should fail
        assertThatThrownBy(() -> coachCodeService.validateAndUseCoachCode("ONCEONLY", UUID.randomUUID()))
                .isInstanceOf(InvalidCoachCodeException.class)
                .hasMessageContaining("already been used");
    }
}
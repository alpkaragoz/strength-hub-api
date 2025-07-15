package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.request.coach.CoachRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.request.coach.CoachUpdateRequest;
import com.strengthhub.strength_hub_api.dto.response.coach.CoachDetailResponse;
import com.strengthhub.strength_hub_api.dto.response.coach.CoachResponse;
import com.strengthhub.strength_hub_api.dto.response.coach.CoachSummaryResponse;
import com.strengthhub.strength_hub_api.dto.response.lifter.LifterSummaryResponse;
import com.strengthhub.strength_hub_api.exception.coach.CoachAlreadyExistsException;
import com.strengthhub.strength_hub_api.exception.coach.CoachNotFoundException;
import com.strengthhub.strength_hub_api.exception.coach.InvalidCoachAssignmentException;
import com.strengthhub.strength_hub_api.exception.coach.InvalidCoachCodeException;
import com.strengthhub.strength_hub_api.exception.lifter.LifterNotFoundException;
import com.strengthhub.strength_hub_api.exception.user.UserNotFoundException;
import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.Lifter;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.CoachRepository;
import com.strengthhub.strength_hub_api.repository.LifterRepository;
import com.strengthhub.strength_hub_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoachService Tests")
class CoachServiceTest {

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LifterRepository lifterRepository;

    @Mock
    private CoachCodeService coachCodeService;

    @InjectMocks
    private CoachService coachService;

    private User testUser;
    private Coach testCoach;
    private Lifter testLifter;
    private UUID testUserId;
    private UUID testCoachId;
    private UUID testLifterId;
    private CoachRegistrationRequest validCoachRegistrationRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCoachId = UUID.randomUUID();
        testLifterId = UUID.randomUUID();

        testUser = User.builder()
                .userId(testUserId)
                .username("coachuser")
                .email("coach@example.com")
                .passwordHash("encoded_password")
                .firstName("Coach")
                .lastName("User")
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        testCoach = Coach.builder()
                .coachId(testCoachId)
                .app_user(testUser)
                .bio("Experienced powerlifting coach")
                .certifications("USAPL Certified")
                .lifters(new ArrayList<>())
                .build();

        User lifterUser = User.builder()
                .userId(testLifterId)
                .username("lifteruser")
                .email("lifter@example.com")
                .passwordHash("encoded_password")
                .firstName("Lifter")
                .lastName("User")
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        testLifter = Lifter.builder()
                .lifterId(testLifterId)
                .app_user(lifterUser)
                .build();

        validCoachRegistrationRequest = CoachRegistrationRequest.builder()
                .bio("Experienced powerlifting coach")
                .certifications("USAPL Certified")
                .coachCode("VALID123")
                .build();
    }

    @Test
    @DisplayName("Should create coach successfully with valid request and code")
    void createCoach_WithValidRequestAndCode_ShouldCreateCoach() {
        // Given
        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(coachRepository.existsById(testUserId)).willReturn(false);
        given(coachRepository.save(any(Coach.class))).willReturn(testCoach);

        // When
        CoachResponse result = coachService.createCoach(testUserId, validCoachRegistrationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCoachId()).isEqualTo(testCoachId);
        assertThat(result.getUsername()).isEqualTo("coachuser");
        assertThat(result.getBio()).isEqualTo("Experienced powerlifting coach");
        assertThat(result.getCertifications()).isEqualTo("USAPL Certified");
        assertThat(result.getLifterCount()).isEqualTo(0);

        then(coachCodeService).should().validateAndUseCoachCode("VALID123", testUserId);
        then(coachRepository).should().save(any(Coach.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void createCoach_WithInvalidUserId_ShouldThrowUserNotFoundException() {
        // Given
        UUID invalidUserId = UUID.randomUUID();
        given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> coachService.createCoach(invalidUserId, validCoachRegistrationRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(invalidUserId.toString());

        then(coachRepository).should(never()).save(any(Coach.class));
    }

    @Test
    @DisplayName("Should throw exception when coach already exists for user")
    void createCoach_WithExistingCoach_ShouldThrowCoachAlreadyExistsException() {
        // Given
        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(coachRepository.existsById(testUserId)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> coachService.createCoach(testUserId, validCoachRegistrationRequest))
                .isInstanceOf(CoachAlreadyExistsException.class)
                .hasMessageContaining(testUserId.toString());

        then(coachRepository).should(never()).save(any(Coach.class));
    }

    @Test
    @DisplayName("Should throw exception when coach code is empty")
    void createCoach_WithEmptyCoachCode_ShouldThrowInvalidCoachCodeException() {
        // Given
        CoachRegistrationRequest requestWithEmptyCode = CoachRegistrationRequest.builder()
                .bio("Test bio")
                .certifications("Test cert")
                .coachCode("")
                .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(coachRepository.existsById(testUserId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> coachService.createCoach(testUserId, requestWithEmptyCode))
                .isInstanceOf(InvalidCoachCodeException.class)
                .hasMessageContaining("cannot be null or empty");

        then(coachRepository).should(never()).save(any(Coach.class));
    }

    @Test
    @DisplayName("Should get coach by ID successfully")
    void getCoachById_WithValidId_ShouldReturnCoach() {
        // Given
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));

        // When
        CoachDetailResponse result = coachService.getCoachById(testCoachId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCoachId()).isEqualTo(testCoachId);
        assertThat(result.getUsername()).isEqualTo("coachuser");
        assertThat(result.getBio()).isEqualTo("Experienced powerlifting coach");
        assertThat(result.getLifters()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when coach not found")
    void getCoachById_WithInvalidId_ShouldThrowCoachNotFoundException() {
        // Given
        UUID invalidId = UUID.randomUUID();
        given(coachRepository.findById(invalidId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> coachService.getCoachById(invalidId))
                .isInstanceOf(CoachNotFoundException.class)
                .hasMessageContaining(invalidId.toString());
    }

    @Test
    @DisplayName("Should get all coaches successfully")
    void getAllCoaches_ShouldReturnAllCoaches() {
        // Given
        Coach coach2 = Coach.builder()
                .coachId(UUID.randomUUID())
                .app_user(testUser)
                .bio("Another coach")
                .certifications("Certified")
                .lifters(new ArrayList<>())
                .build();

        List<Coach> coaches = Arrays.asList(testCoach, coach2);
        given(coachRepository.findAll()).willReturn(coaches);

        // When
        List<CoachResponse> result = coachService.getAllCoaches();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBio()).isEqualTo("Experienced powerlifting coach");
        assertThat(result.get(1).getBio()).isEqualTo("Another coach");
    }

    @Test
    @DisplayName("Should update coach successfully")
    void updateCoach_WithValidRequest_ShouldUpdateCoach() {
        // Given
        CoachUpdateRequest updateRequest = CoachUpdateRequest.builder()
                .bio("Updated bio")
                .certifications("Updated certifications")
                .build();

        Coach updatedCoach = Coach.builder()
                .coachId(testCoachId)
                .app_user(testUser)
                .bio("Updated bio")
                .certifications("Updated certifications")
                .lifters(new ArrayList<>())
                .build();

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(coachRepository.save(any(Coach.class))).willReturn(updatedCoach);

        // When
        CoachResponse result = coachService.updateCoach(testCoachId, updateRequest);

        // Then
        assertThat(result.getBio()).isEqualTo("Updated bio");
        assertThat(result.getCertifications()).isEqualTo("Updated certifications");
    }

    @Test
    @DisplayName("Should delete coach successfully")
    void deleteCoach_WithValidId_ShouldDeleteCoach() {
        // Given
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));

        // When
        coachService.deleteCoach(testCoachId);

        // Then
        then(coachRepository).should().delete(testCoach);
    }

    @Test
    @DisplayName("Should get coach lifters successfully")
    void getCoachLifters_WithValidId_ShouldReturnLifters() {
        // Given
        testCoach.getLifters().add(testLifter);
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));

        // When
        List<LifterSummaryResponse> result = coachService.getCoachLifters(testCoachId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLifterId()).isEqualTo(testLifterId);
        assertThat(result.get(0).getUsername()).isEqualTo("lifteruser");
    }

    @Test
    @DisplayName("Should assign lifter to coach successfully")
    void assignLifterToCoach_WithValidIds_ShouldAssignLifter() {
        // Given
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(coachRepository.save(any(Coach.class))).willReturn(testCoach);

        // When
        coachService.assignLifterToCoach(testCoachId, testLifterId);

        // Then
        then(coachRepository).should().save(testCoach);
        assertThat(testCoach.getLifters()).contains(testLifter);
        assertThat(testLifter.getCoach()).isEqualTo(testCoach);
    }

    @Test
    @DisplayName("Should reassign lifter from previous coach")
    void assignLifterToCoach_WithLifterHavingPreviousCoach_ShouldReassign() {
        // Given
        Coach previousCoach = Coach.builder()
                .coachId(UUID.randomUUID())
                .app_user(testUser)
                .lifters(new ArrayList<>())
                .build();

        testLifter.setCoach(previousCoach);
        previousCoach.getLifters().add(testLifter);

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(coachRepository.save(any(Coach.class))).willReturn(testCoach);

        // When
        coachService.assignLifterToCoach(testCoachId, testLifterId);

        // Then
        assertThat(testLifter.getCoach()).isEqualTo(testCoach);
        assertThat(previousCoach.getLifters()).doesNotContain(testLifter);
        assertThat(testCoach.getLifters()).contains(testLifter);
    }

    @Test
    @DisplayName("Should throw exception when assigning non-existent lifter")
    void assignLifterToCoach_WithInvalidLifterId_ShouldThrowLifterNotFoundException() {
        // Given
        UUID invalidLifterId = UUID.randomUUID();
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.findById(invalidLifterId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> coachService.assignLifterToCoach(testCoachId, invalidLifterId))
                .isInstanceOf(LifterNotFoundException.class)
                .hasMessageContaining(invalidLifterId.toString());
    }

    @Test
    @DisplayName("Should remove lifter from coach successfully")
    void removeLifterFromCoach_WithValidIds_ShouldRemoveLifter() {
        // Given
        testLifter.setCoach(testCoach);
        testCoach.getLifters().add(testLifter);

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(coachRepository.save(any(Coach.class))).willReturn(testCoach);

        // When
        coachService.removeLifterFromCoach(testCoachId, testLifterId);

        // Then
        assertThat(testLifter.getCoach()).isNull();
        assertThat(testCoach.getLifters()).doesNotContain(testLifter);
    }

    @Test
    @DisplayName("Should throw exception when removing lifter not assigned to coach")
    void removeLifterFromCoach_WithLifterNotAssignedToCoach_ShouldThrowException() {
        // Given
        testLifter.setCoach(null); // Not assigned to any coach

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));

        // When & Then
        assertThatThrownBy(() -> coachService.removeLifterFromCoach(testCoachId, testLifterId))
                .isInstanceOf(InvalidCoachAssignmentException.class)
                .hasMessageContaining("not assigned to this coach");
    }

    @Test
    @DisplayName("Should map to summary response correctly")
    void mapToSummaryResponse_WithValidCoach_ShouldReturnSummaryResponse() {
        // When
        CoachSummaryResponse result = coachService.mapToSummaryResponse(testCoach);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCoachId()).isEqualTo(testCoachId);
        assertThat(result.getFirstName()).isEqualTo("Coach");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getUsername()).isEqualTo("coachuser");
    }

    @Test
    @DisplayName("Should handle empty bio and certifications")
    void createCoach_WithEmptyBioAndCertifications_ShouldUseDefaults() {
        // Given
        CoachRegistrationRequest requestWithNulls = CoachRegistrationRequest.builder()
                .coachCode("VALID123")
                .build();

        Coach coachWithDefaults = Coach.builder()
                .coachId(testCoachId)
                .app_user(testUser)
                .bio("")
                .certifications("")
                .lifters(new ArrayList<>())
                .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(coachRepository.existsById(testUserId)).willReturn(false);
        given(coachRepository.save(any(Coach.class))).willReturn(coachWithDefaults);

        // When
        CoachResponse result = coachService.createCoach(testUserId, requestWithNulls);

        // Then
        assertThat(result.getBio()).isEqualTo("");
        assertThat(result.getCertifications()).isEqualTo("");
    }

    @Test
    @DisplayName("Should update only provided fields in coach update")
    void updateCoach_WithPartialRequest_ShouldUpdateOnlyProvidedFields() {
        // Given
        CoachUpdateRequest partialUpdateRequest = CoachUpdateRequest.builder()
                .bio("Only bio updated")
                .build();

        Coach updatedCoach = Coach.builder()
                .coachId(testCoachId)
                .app_user(testUser)
                .bio("Only bio updated")
                .certifications("USAPL Certified") // Original value
                .lifters(new ArrayList<>())
                .build();

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(coachRepository.save(any(Coach.class))).willReturn(updatedCoach);

        // When
        CoachResponse result = coachService.updateCoach(testCoachId, partialUpdateRequest);

        // Then
        assertThat(result.getBio()).isEqualTo("Only bio updated");
        assertThat(result.getCertifications()).isEqualTo("USAPL Certified");
    }
}
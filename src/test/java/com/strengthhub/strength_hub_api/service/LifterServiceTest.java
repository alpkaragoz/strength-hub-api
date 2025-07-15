package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.response.coach.CoachSummaryResponse;
import com.strengthhub.strength_hub_api.dto.response.lifter.LifterResponse;
import com.strengthhub.strength_hub_api.exception.coach.CoachNotFoundException;
import com.strengthhub.strength_hub_api.exception.lifter.LifterNotFoundException;
import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.Lifter;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.CoachRepository;
import com.strengthhub.strength_hub_api.repository.LifterRepository;
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
@DisplayName("LifterService Tests")
class LifterServiceTest {

    @Mock
    private LifterRepository lifterRepository;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private CoachService coachService;

    @InjectMocks
    private LifterService lifterService;

    private Lifter testLifter;
    private Coach testCoach;
    private User lifterUser;
    private User coachUser;
    private UUID testLifterId;
    private UUID testCoachId;
    private CoachSummaryResponse testCoachSummary;

    @BeforeEach
    void setUp() {
        testLifterId = UUID.randomUUID();
        testCoachId = UUID.randomUUID();

        lifterUser = User.builder()
                .userId(testLifterId)
                .username("lifteruser")
                .email("lifter@example.com")
                .passwordHash("encoded_password")
                .firstName("Lifter")
                .lastName("User")
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        coachUser = User.builder()
                .userId(testCoachId)
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
                .app_user(coachUser)
                .bio("Experienced coach")
                .lifters(new ArrayList<>())
                .build();

        testLifter = Lifter.builder()
                .lifterId(testLifterId)
                .app_user(lifterUser)
                .coach(null)
                .build();

        testCoachSummary = CoachSummaryResponse.builder()
                .coachId(testCoachId)
                .firstName("Coach")
                .lastName("User")
                .username("coachuser")
                .build();
    }

    @Test
    @DisplayName("Should get lifter by ID successfully")
    void getLifterById_WithValidId_ShouldReturnLifter() {
        // Given
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));

        // When
        LifterResponse result = lifterService.getLifterById(testLifterId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLifterId()).isEqualTo(testLifterId);
        assertThat(result.getUsername()).isEqualTo("lifteruser");
        assertThat(result.getEmail()).isEqualTo("lifter@example.com");
        assertThat(result.getFirstName()).isEqualTo("Lifter");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getCoach()).isNull();
    }

    @Test
    @DisplayName("Should get lifter by ID with coach successfully")
    void getLifterById_WithCoach_ShouldReturnLifterWithCoach() {
        // Given
        testLifter.setCoach(testCoach);
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(coachService.mapToSummaryResponse(testCoach)).willReturn(testCoachSummary);

        // When
        LifterResponse result = lifterService.getLifterById(testLifterId);

        // Then
        assertThat(result.getCoach()).isNotNull();
        assertThat(result.getCoach().getCoachId()).isEqualTo(testCoachId);
        assertThat(result.getCoach().getUsername()).isEqualTo("coachuser");
    }

    @Test
    @DisplayName("Should throw exception when lifter not found")
    void getLifterById_WithInvalidId_ShouldThrowLifterNotFoundException() {
        // Given
        UUID invalidId = UUID.randomUUID();
        given(lifterRepository.findById(invalidId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lifterService.getLifterById(invalidId))
                .isInstanceOf(LifterNotFoundException.class)
                .hasMessageContaining(invalidId.toString());
    }

    @Test
    @DisplayName("Should get all lifters successfully")
    void getAllLifters_ShouldReturnAllLifters() {
        // Given
        Lifter lifter2 = Lifter.builder()
                .lifterId(UUID.randomUUID())
                .app_user(User.builder()
                        .userId(UUID.randomUUID())
                        .username("lifter2")
                        .email("lifter2@example.com")
                        .firstName("Lifter2")
                        .lastName("User2")
                        .createdAt(LocalDateTime.now())
                        .build())
                .build();

        List<Lifter> lifters = Arrays.asList(testLifter, lifter2);
        given(lifterRepository.findAll()).willReturn(lifters);

        // When
        List<LifterResponse> result = lifterService.getAllLifters();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("lifteruser");
        assertThat(result.get(1).getUsername()).isEqualTo("lifter2");
    }

    @Test
    @DisplayName("Should delete lifter successfully")
    void deleteLifter_WithValidId_ShouldDeleteLifter() {
        // Given
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));

        // When
        lifterService.deleteLifter(testLifterId);

        // Then
        then(lifterRepository).should().delete(testLifter);
    }

    @Test
    @DisplayName("Should delete lifter with coach successfully")
    void deleteLifter_WithCoach_ShouldRemoveFromCoachAndDelete() {
        // Given
        testLifter.setCoach(testCoach);
        testCoach.getLifters().add(testLifter);

        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));

        // When
        lifterService.deleteLifter(testLifterId);

        // Then
        assertThat(testCoach.getLifters()).doesNotContain(testLifter);
        then(lifterRepository).should().delete(testLifter);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent lifter")
    void deleteLifter_WithInvalidId_ShouldThrowLifterNotFoundException() {
        // Given
        UUID invalidId = UUID.randomUUID();
        given(lifterRepository.findById(invalidId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lifterService.deleteLifter(invalidId))
                .isInstanceOf(LifterNotFoundException.class)
                .hasMessageContaining(invalidId.toString());

        then(lifterRepository).should(never()).delete(any(Lifter.class));
    }

    @Test
    @DisplayName("Should assign coach to lifter successfully")
    void assignCoachToLifter_WithValidIds_ShouldAssignCoach() {
        // Given
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.save(any(Lifter.class))).willReturn(testLifter);
        given(coachService.mapToSummaryResponse(testCoach)).willReturn(testCoachSummary);

        // When
        LifterResponse result = lifterService.assignCoachToLifter(testLifterId, testCoachId);

        // Then
        assertThat(result.getCoach()).isNotNull();
        assertThat(result.getCoach().getCoachId()).isEqualTo(testCoachId);
        assertThat(testLifter.getCoach()).isEqualTo(testCoach);
        assertThat(testCoach.getLifters()).contains(testLifter);
        then(lifterRepository).should().save(testLifter);
    }

    @Test
    @DisplayName("Should reassign lifter from previous coach")
    void assignCoachToLifter_WithPreviousCoach_ShouldReassign() {
        // Given
        Coach previousCoach = Coach.builder()
                .coachId(UUID.randomUUID())
                .app_user(coachUser)
                .lifters(new ArrayList<>())
                .build();

        testLifter.setCoach(previousCoach);
        previousCoach.getLifters().add(testLifter);

        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.save(any(Lifter.class))).willReturn(testLifter);
        given(coachService.mapToSummaryResponse(testCoach)).willReturn(testCoachSummary);

        // When
        LifterResponse result = lifterService.assignCoachToLifter(testLifterId, testCoachId);

        // Then
        assertThat(testLifter.getCoach()).isEqualTo(testCoach);
        assertThat(previousCoach.getLifters()).doesNotContain(testLifter);
        assertThat(testCoach.getLifters()).contains(testLifter);
        assertThat(result.getCoach().getCoachId()).isEqualTo(testCoachId);
    }

    @Test
    @DisplayName("Should throw exception when assigning non-existent coach")
    void assignCoachToLifter_WithInvalidCoachId_ShouldThrowCoachNotFoundException() {
        // Given
        UUID invalidCoachId = UUID.randomUUID();
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(coachRepository.findById(invalidCoachId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lifterService.assignCoachToLifter(testLifterId, invalidCoachId))
                .isInstanceOf(CoachNotFoundException.class)
                .hasMessageContaining(invalidCoachId.toString());

        then(lifterRepository).should(never()).save(any(Lifter.class));
    }

    @Test
    @DisplayName("Should throw exception when assigning to non-existent lifter")
    void assignCoachToLifter_WithInvalidLifterId_ShouldThrowLifterNotFoundException() {
        // Given
        UUID invalidLifterId = UUID.randomUUID();
        given(lifterRepository.findById(invalidLifterId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lifterService.assignCoachToLifter(invalidLifterId, testCoachId))
                .isInstanceOf(LifterNotFoundException.class)
                .hasMessageContaining(invalidLifterId.toString());

        then(coachRepository).should(never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should remove coach from lifter successfully")
    void removeCoachFromLifter_WithCoach_ShouldRemoveCoach() {
        // Given
        testLifter.setCoach(testCoach);
        testCoach.getLifters().add(testLifter);

        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(lifterRepository.save(any(Lifter.class))).willReturn(testLifter);

        // When
        LifterResponse result = lifterService.removeCoachFromLifter(testLifterId);

        // Then
        assertThat(testLifter.getCoach()).isNull();
        assertThat(testCoach.getLifters()).doesNotContain(testLifter);
        assertThat(result.getCoach()).isNull();
        then(lifterRepository).should().save(testLifter);
    }

    @Test
    @DisplayName("Should handle removing coach from lifter with no coach")
    void removeCoachFromLifter_WithNoCoach_ShouldHandleGracefully() {
        // Given - testLifter has no coach by default
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));

        // When
        LifterResponse result = lifterService.removeCoachFromLifter(testLifterId);

        // Then
        assertThat(result.getCoach()).isNull();
        then(lifterRepository).should(never()).save(any(Lifter.class));
    }

    @Test
    @DisplayName("Should throw exception when removing coach from non-existent lifter")
    void removeCoachFromLifter_WithInvalidLifterId_ShouldThrowLifterNotFoundException() {
        // Given
        UUID invalidLifterId = UUID.randomUUID();
        given(lifterRepository.findById(invalidLifterId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lifterService.removeCoachFromLifter(invalidLifterId))
                .isInstanceOf(LifterNotFoundException.class)
                .hasMessageContaining(invalidLifterId.toString());
    }

    @Test
    @DisplayName("Should get lifters without coach successfully")
    void getLiftersWithoutCoach_ShouldReturnLiftersWithoutCoach() {
        // Given
        Lifter lifterWithoutCoach = Lifter.builder()
                .lifterId(UUID.randomUUID())
                .app_user(User.builder()
                        .userId(UUID.randomUUID())
                        .username("lonelifter")
                        .email("lone@example.com")
                        .firstName("Lone")
                        .lastName("Lifter")
                        .createdAt(LocalDateTime.now())
                        .build())
                .coach(null)
                .build();

        Lifter lifterWithCoach = Lifter.builder()
                .lifterId(UUID.randomUUID())
                .app_user(User.builder()
                        .userId(UUID.randomUUID())
                        .username("coachedlifter")
                        .email("coached@example.com")
                        .firstName("Coached")
                        .lastName("Lifter")
                        .createdAt(LocalDateTime.now())
                        .build())
                .coach(testCoach)
                .build();

        List<Lifter> allLifters = Arrays.asList(lifterWithoutCoach, lifterWithCoach);
        given(lifterRepository.findAll()).willReturn(allLifters);

        // When
        List<LifterResponse> result = lifterService.getLiftersWithoutCoach();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("lonelifter");
        assertThat(result.get(0).getCoach()).isNull();
    }

    @Test
    @DisplayName("Should get lifters by coach successfully")
    void getLiftersByCoach_WithValidCoachId_ShouldReturnCoachLifters() {
        // Given
        testLifter.setCoach(testCoach);
        testCoach.getLifters().add(testLifter);

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(coachService.mapToSummaryResponse(testCoach)).willReturn(testCoachSummary);

        // When
        List<LifterResponse> result = lifterService.getLiftersByCoach(testCoachId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLifterId()).isEqualTo(testLifterId);
        assertThat(result.get(0).getCoach().getCoachId()).isEqualTo(testCoachId);
    }

    @Test
    @DisplayName("Should throw exception when getting lifters by non-existent coach")
    void getLiftersByCoach_WithInvalidCoachId_ShouldThrowCoachNotFoundException() {
        // Given
        UUID invalidCoachId = UUID.randomUUID();
        given(coachRepository.findById(invalidCoachId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lifterService.getLiftersByCoach(invalidCoachId))
                .isInstanceOf(CoachNotFoundException.class)
                .hasMessageContaining(invalidCoachId.toString());
    }

    @Test
    @DisplayName("Should return empty list when coach has no lifters")
    void getLiftersByCoach_WithCoachWithoutLifters_ShouldReturnEmptyList() {
        // Given
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));

        // When
        List<LifterResponse> result = lifterService.getLiftersByCoach(testCoachId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple lifters for same coach")
    void getLiftersByCoach_WithMultipleLifters_ShouldReturnAllLifters() {
        // Given
        Lifter lifter2 = Lifter.builder()
                .lifterId(UUID.randomUUID())
                .app_user(User.builder()
                        .userId(UUID.randomUUID())
                        .username("lifter2")
                        .email("lifter2@example.com")
                        .firstName("Lifter2")
                        .lastName("User2")
                        .createdAt(LocalDateTime.now())
                        .build())
                .coach(testCoach)
                .build();

        testLifter.setCoach(testCoach);
        testCoach.getLifters().add(testLifter);
        testCoach.getLifters().add(lifter2);

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(coachService.mapToSummaryResponse(testCoach)).willReturn(testCoachSummary);

        // When
        List<LifterResponse> result = lifterService.getLiftersByCoach(testCoachId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(LifterResponse::getUsername))
                .containsExactlyInAnyOrder("lifteruser", "lifter2");
    }

    @Test
    @DisplayName("Should preserve lifter data when assigning coach")
    void assignCoachToLifter_ShouldPreserveLifterData() {
        // Given
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.save(any(Lifter.class))).willReturn(testLifter);
        given(coachService.mapToSummaryResponse(testCoach)).willReturn(testCoachSummary);

        // When
        LifterResponse result = lifterService.assignCoachToLifter(testLifterId, testCoachId);

        // Then
        assertThat(result.getLifterId()).isEqualTo(testLifterId);
        assertThat(result.getUsername()).isEqualTo("lifteruser");
        assertThat(result.getEmail()).isEqualTo("lifter@example.com");
        assertThat(result.getFirstName()).isEqualTo("Lifter");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getCoach().getCoachId()).isEqualTo(testCoachId);
    }
}
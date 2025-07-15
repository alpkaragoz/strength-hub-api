package com.strengthhub.strength_hub_api.service.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutPlanAssignmentRequest;
import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutPlanCreateRequest;
import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutPlanUpdateRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutPlanDetailResponse;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutPlanResponse;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutPlanSummaryResponse;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutStatsResponse;
import com.strengthhub.strength_hub_api.exception.coach.CoachNotFoundException;
import com.strengthhub.strength_hub_api.exception.lifter.LifterNotFoundException;
import com.strengthhub.strength_hub_api.exception.workout.InvalidWorkoutStructureException;
import com.strengthhub.strength_hub_api.exception.workout.WorkoutPlanAlreadyAssignedException;
import com.strengthhub.strength_hub_api.exception.workout.WorkoutPlanNotFoundException;
import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.Lifter;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.model.workout.Exercise;
import com.strengthhub.strength_hub_api.model.workout.WorkoutDay;
import com.strengthhub.strength_hub_api.model.workout.WorkoutPlan;
import com.strengthhub.strength_hub_api.model.workout.WorkoutWeek;
import com.strengthhub.strength_hub_api.repository.CoachRepository;
import com.strengthhub.strength_hub_api.repository.LifterRepository;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutPlanRepository;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutSetRepository;
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
@DisplayName("WorkoutPlanService Tests")
class WorkoutPlanServiceTest {

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private LifterRepository lifterRepository;

    @Mock
    private WorkoutSetRepository workoutSetRepository;

    @InjectMocks
    private WorkoutPlanService workoutPlanService;

    private Coach testCoach;
    private Lifter testLifter;
    private User coachUser;
    private User lifterUser;
    private WorkoutPlan testWorkoutPlan;
    private WorkoutPlanCreateRequest validCreateRequest;
    private UUID testCoachId;
    private UUID testLifterId;
    private UUID testPlanId;

    @BeforeEach
    void setUp() {
        testCoachId = UUID.randomUUID();
        testLifterId = UUID.randomUUID();
        testPlanId = UUID.randomUUID();

        coachUser = User.builder()
                .userId(testCoachId)
                .username("coach")
                .email("coach@example.com")
                .firstName("Coach")
                .lastName("User")
                .build();

        lifterUser = User.builder()
                .userId(testLifterId)
                .username("lifter")
                .email("lifter@example.com")
                .firstName("Lifter")
                .lastName("User")
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
                .build();

        testWorkoutPlan = WorkoutPlan.builder()
                .planId(testPlanId)
                .name("Test Plan")
                .description("Test Description")
                .totalWeeks(8)
                .coach(testCoach)
                .assignedLifter(null)
                .isActive(true)
                .isTemplate(false)
                .createdAt(LocalDateTime.now())
                .weeks(new ArrayList<>())
                .build();

        validCreateRequest = WorkoutPlanCreateRequest.builder()
                .name("Test Plan")
                .description("Test Description")
                .totalWeeks(8)
                .coachId(testCoachId)
                .assignedLifterId(null)
                .isTemplate(false)
                .build();
    }

    @Test
    @DisplayName("Should create workout plan successfully without assigned lifter")
    void createWorkoutPlan_WithValidRequestWithoutLifter_ShouldCreatePlan() {
        // Given
        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(workoutPlanRepository.save(any(WorkoutPlan.class))).willReturn(testWorkoutPlan);

        // When
        WorkoutPlanResponse result = workoutPlanService.createWorkoutPlan(validCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPlanId()).isEqualTo(testPlanId);
        assertThat(result.getName()).isEqualTo("Test Plan");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getTotalWeeks()).isEqualTo(8);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getIsTemplate()).isFalse();
        assertThat(result.getAssignedLifter()).isNull();
        assertThat(result.getCoach().getCoachId()).isEqualTo(testCoachId);

        then(workoutPlanRepository).should().save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("Should create workout plan successfully with assigned lifter")
    void createWorkoutPlan_WithValidRequestWithLifter_ShouldCreatePlanWithAssignment() {
        // Given
        validCreateRequest.setAssignedLifterId(testLifterId);
        testWorkoutPlan.setAssignedLifter(testLifter);

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(workoutPlanRepository.findActiveByLifterId(testLifterId)).willReturn(Optional.empty());
        given(workoutPlanRepository.save(any(WorkoutPlan.class))).willReturn(testWorkoutPlan);

        // When
        WorkoutPlanResponse result = workoutPlanService.createWorkoutPlan(validCreateRequest);

        // Then
        assertThat(result.getAssignedLifter()).isNotNull();
        assertThat(result.getAssignedLifter().getLifterId()).isEqualTo(testLifterId);
    }

    @Test
    @DisplayName("Should throw exception when coach not found")
    void createWorkoutPlan_WithInvalidCoachId_ShouldThrowCoachNotFoundException() {
        // Given
        UUID invalidCoachId = UUID.randomUUID();
        validCreateRequest.setCoachId(invalidCoachId);

        given(coachRepository.findById(invalidCoachId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutPlanService.createWorkoutPlan(validCreateRequest))
                .isInstanceOf(CoachNotFoundException.class)
                .hasMessageContaining(invalidCoachId.toString());

        then(workoutPlanRepository).should(never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("Should throw exception when lifter not found")
    void createWorkoutPlan_WithInvalidLifterId_ShouldThrowLifterNotFoundException() {
        // Given
        UUID invalidLifterId = UUID.randomUUID();
        validCreateRequest.setAssignedLifterId(invalidLifterId);

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.findById(invalidLifterId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutPlanService.createWorkoutPlan(validCreateRequest))
                .isInstanceOf(LifterNotFoundException.class)
                .hasMessageContaining(invalidLifterId.toString());

        then(workoutPlanRepository).should(never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("Should throw exception when lifter already has active plan")
    void createWorkoutPlan_WithLifterHavingActivePlan_ShouldThrowWorkoutPlanAlreadyAssignedException() {
        // Given
        validCreateRequest.setAssignedLifterId(testLifterId);
        WorkoutPlan existingPlan = WorkoutPlan.builder().planId(UUID.randomUUID()).build();

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(workoutPlanRepository.findActiveByLifterId(testLifterId)).willReturn(Optional.of(existingPlan));

        // When & Then
        assertThatThrownBy(() -> workoutPlanService.createWorkoutPlan(validCreateRequest))
                .isInstanceOf(WorkoutPlanAlreadyAssignedException.class)
                .hasMessageContaining(testLifterId.toString());

        then(workoutPlanRepository).should(never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("Should get workout plan by ID successfully")
    void getWorkoutPlanById_WithValidId_ShouldReturnDetailResponse() {
        // Given
        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutSetRepository.countByPlanId(testPlanId)).willReturn(10L);
        given(workoutSetRepository.countCompletedByPlanId(testPlanId)).willReturn(5L);

        // When
        WorkoutPlanDetailResponse result = workoutPlanService.getWorkoutPlanById(testPlanId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPlanId()).isEqualTo(testPlanId);
        assertThat(result.getName()).isEqualTo("Test Plan");
        assertThat(result.getStats()).isNotNull();
        assertThat(result.getStats().getTotalSets()).isEqualTo(10);
        assertThat(result.getStats().getCompletedSets()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should throw exception when workout plan not found")
    void getWorkoutPlanById_WithInvalidId_ShouldThrowWorkoutPlanNotFoundException() {
        // Given
        UUID invalidId = UUID.randomUUID();
        given(workoutPlanRepository.findById(invalidId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutPlanService.getWorkoutPlanById(invalidId))
                .isInstanceOf(WorkoutPlanNotFoundException.class)
                .hasMessageContaining(invalidId.toString());
    }

    @Test
    @DisplayName("Should get workout plans by coach successfully")
    void getWorkoutPlansByCoach_WithValidCoachId_ShouldReturnPlans() {
        // Given
        WorkoutPlan plan2 = WorkoutPlan.builder()
                .planId(UUID.randomUUID())
                .name("Plan 2")
                .coach(testCoach)
                .weeks(new ArrayList<>())
                .build();

        List<WorkoutPlan> plans = Arrays.asList(testWorkoutPlan, plan2);

        given(coachRepository.findById(testCoachId)).willReturn(Optional.of(testCoach));
        given(workoutPlanRepository.findByCoach_CoachId(testCoachId)).willReturn(plans);

        // When
        List<WorkoutPlanSummaryResponse> result = workoutPlanService.getWorkoutPlansByCoach(testCoachId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Test Plan");
        assertThat(result.get(1).getName()).isEqualTo("Plan 2");
    }

    @Test
    @DisplayName("Should get workout plans by lifter successfully")
    void getWorkoutPlansByLifter_WithValidLifterId_ShouldReturnPlans() {
        // Given
        testWorkoutPlan.setAssignedLifter(testLifter);
        List<WorkoutPlan> plans = Arrays.asList(testWorkoutPlan);

        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(workoutPlanRepository.findByAssignedLifter_LifterId(testLifterId)).willReturn(plans);

        // When
        List<WorkoutPlanSummaryResponse> result = workoutPlanService.getWorkoutPlansByLifter(testLifterId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssignedLifter().getLifterId()).isEqualTo(testLifterId);
    }

    @Test
    @DisplayName("Should update workout plan successfully")
    void updateWorkoutPlan_WithValidRequest_ShouldUpdatePlan() {
        // Given
        WorkoutPlanUpdateRequest updateRequest = WorkoutPlanUpdateRequest.builder()
                .name("Updated Plan")
                .description("Updated Description")
                .isActive(false)
                .isTemplate(true)
                .build();

        WorkoutPlan updatedPlan = WorkoutPlan.builder()
                .planId(testPlanId)
                .name("Updated Plan")
                .description("Updated Description")
                .totalWeeks(8)
                .coach(testCoach)
                .isActive(false)
                .isTemplate(true)
                .createdAt(LocalDateTime.now())
                .weeks(new ArrayList<>())
                .build();

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutPlanRepository.save(any(WorkoutPlan.class))).willReturn(updatedPlan);

        // When
        WorkoutPlanResponse result = workoutPlanService.updateWorkoutPlan(testPlanId, updateRequest);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Plan");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getIsTemplate()).isTrue();
    }

    @Test
    @DisplayName("Should assign lifter to workout plan successfully")
    void assignLifterToWorkoutPlan_WithValidIds_ShouldAssignLifter() {
        // Given
        WorkoutPlanAssignmentRequest assignmentRequest = WorkoutPlanAssignmentRequest.builder()
                .lifterId(testLifterId)
                .build();

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(workoutPlanRepository.findActiveByLifterId(testLifterId)).willReturn(Optional.empty());
        given(workoutPlanRepository.save(any(WorkoutPlan.class))).willReturn(testWorkoutPlan);

        // When
        workoutPlanService.assignLifterToWorkoutPlan(testPlanId, assignmentRequest);

        // Then
        then(workoutPlanRepository).should().save(testWorkoutPlan);
    }

    @Test
    @DisplayName("Should throw exception when assigning to already assigned plan")
    void assignLifterToWorkoutPlan_WithAlreadyAssignedPlan_ShouldThrowException() {
        // Given
        testWorkoutPlan.setAssignedLifter(testLifter); // Already assigned

        WorkoutPlanAssignmentRequest assignmentRequest = WorkoutPlanAssignmentRequest.builder()
                .lifterId(UUID.randomUUID()) // Different lifter
                .build();

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));

        // When & Then
        assertThatThrownBy(() -> workoutPlanService.assignLifterToWorkoutPlan(testPlanId, assignmentRequest))
                .isInstanceOf(WorkoutPlanAlreadyAssignedException.class);

        then(workoutPlanRepository).should(never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("Should unassign lifter from workout plan successfully")
    void unassignLifterFromWorkoutPlan_WithAssignedPlan_ShouldUnassignLifter() {
        // Given
        testWorkoutPlan.setAssignedLifter(testLifter);

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutPlanRepository.save(any(WorkoutPlan.class))).willReturn(testWorkoutPlan);

        // When
        workoutPlanService.unassignLifterFromWorkoutPlan(testPlanId);

        // Then
        then(workoutPlanRepository).should().save(testWorkoutPlan);
    }

    @Test
    @DisplayName("Should throw exception when unassigning from unassigned plan")
    void unassignLifterFromWorkoutPlan_WithUnassignedPlan_ShouldThrowException() {
        // Given - testWorkoutPlan has no assigned lifter by default

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));

        // When & Then
        assertThatThrownBy(() -> workoutPlanService.unassignLifterFromWorkoutPlan(testPlanId))
                .isInstanceOf(InvalidWorkoutStructureException.class)
                .hasMessageContaining("No lifter is currently assigned");

        then(workoutPlanRepository).should(never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("Should delete workout plan successfully")
    void deleteWorkoutPlan_WithValidId_ShouldDeletePlan() {
        // Given
        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));

        // When
        workoutPlanService.deleteWorkoutPlan(testPlanId);

        // Then
        then(workoutPlanRepository).should().delete(testWorkoutPlan);
    }

    @Test
    @DisplayName("Should get workout plan stats successfully")
    void getWorkoutPlanStats_WithValidId_ShouldReturnStats() {
        // Given
        // Create a plan with nested structure for stats calculation
        WorkoutWeek week1 = WorkoutWeek.builder()
                .weekId(UUID.randomUUID())
                .weekNumber(1)
                .workoutPlan(testWorkoutPlan)
                .days(new ArrayList<>())
                .build();

        WorkoutDay day1 = WorkoutDay.builder()
                .dayId(UUID.randomUUID())
                .dayNumber(1)
                .name("Day 1")
                .workoutWeek(week1)
                .exercises(new ArrayList<>())
                .build();

        Exercise exercise1 = Exercise.builder()
                .exerciseId(UUID.randomUUID())
                .name("Squat")
                .exerciseOrder(1)
                .workoutDay(day1)
                .sets(new ArrayList<>())
                .build();

        day1.getExercises().add(exercise1);
        week1.getDays().add(day1);
        testWorkoutPlan.getWeeks().add(week1);

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutSetRepository.countByPlanId(testPlanId)).willReturn(20L);
        given(workoutSetRepository.countCompletedByPlanId(testPlanId)).willReturn(15L);

        // When
        WorkoutStatsResponse result = workoutPlanService.getWorkoutPlanStats(testPlanId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalWeeks()).isEqualTo(8);
        assertThat(result.getTotalDays()).isEqualTo(1);
        assertThat(result.getTotalExercises()).isEqualTo(1);
        assertThat(result.getTotalSets()).isEqualTo(20);
        assertThat(result.getCompletedSets()).isEqualTo(15);
        assertThat(result.getCompletionPercentage()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Should handle zero sets in stats calculation")
    void getWorkoutPlanStats_WithZeroSets_ShouldReturnZeroCompletion() {
        // Given
        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutSetRepository.countByPlanId(testPlanId)).willReturn(0L);
        given(workoutSetRepository.countCompletedByPlanId(testPlanId)).willReturn(0L);

        // When
        WorkoutStatsResponse result = workoutPlanService.getWorkoutPlanStats(testPlanId);

        // Then
        assertThat(result.getCompletionPercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should prevent assigning lifter who already has active plan")
    void updateWorkoutPlan_WithLifterHavingActivePlan_ShouldThrowException() {
        // Given
        UUID anotherPlanId = UUID.randomUUID();
        WorkoutPlan anotherPlan = WorkoutPlan.builder().planId(anotherPlanId).build();

        WorkoutPlanUpdateRequest updateRequest = WorkoutPlanUpdateRequest.builder()
                .assignedLifterId(testLifterId)
                .build();

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(workoutPlanRepository.findActiveByLifterId(testLifterId)).willReturn(Optional.of(anotherPlan));

        // When & Then
        assertThatThrownBy(() -> workoutPlanService.updateWorkoutPlan(testPlanId, updateRequest))
                .isInstanceOf(WorkoutPlanAlreadyAssignedException.class);
    }

    @Test
    @DisplayName("Should allow reassigning same lifter in update")
    void updateWorkoutPlan_WithSameLifterAssignment_ShouldNotThrowException() {
        // Given
        testWorkoutPlan.setAssignedLifter(testLifter); // Already assigned to this plan

        WorkoutPlanUpdateRequest updateRequest = WorkoutPlanUpdateRequest.builder()
                .assignedLifterId(testLifterId) // Same lifter
                .name("Updated Name")
                .build();

        WorkoutPlan updatedPlan = WorkoutPlan.builder()
                .planId(testPlanId)
                .name("Updated Name")
                .assignedLifter(testLifter)
                .coach(testCoach)
                .weeks(new ArrayList<>())
                .build();

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(lifterRepository.findById(testLifterId)).willReturn(Optional.of(testLifter));
        given(workoutPlanRepository.findActiveByLifterId(testLifterId)).willReturn(Optional.of(testWorkoutPlan)); // Same plan
        given(workoutPlanRepository.save(any(WorkoutPlan.class))).willReturn(updatedPlan);

        // When
        WorkoutPlanResponse result = workoutPlanService.updateWorkoutPlan(testPlanId, updateRequest);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getAssignedLifter().getLifterId()).isEqualTo(testLifterId);
    }
}
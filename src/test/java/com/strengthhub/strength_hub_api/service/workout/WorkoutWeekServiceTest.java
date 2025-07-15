package com.strengthhub.strength_hub_api.service.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutWeekRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutWeekResponse;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutWeekSummaryResponse;
import com.strengthhub.strength_hub_api.exception.workout.*;
import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.model.workout.Exercise;
import com.strengthhub.strength_hub_api.model.workout.WorkoutDay;
import com.strengthhub.strength_hub_api.model.workout.WorkoutPlan;
import com.strengthhub.strength_hub_api.model.workout.WorkoutSet;
import com.strengthhub.strength_hub_api.model.workout.WorkoutWeek;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutPlanRepository;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutWeekRepository;
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
@DisplayName("WorkoutWeekService Tests")
class WorkoutWeekServiceTest {

    @Mock
    private WorkoutWeekRepository workoutWeekRepository;

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @InjectMocks
    private WorkoutWeekService workoutWeekService;

    private WorkoutPlan testWorkoutPlan;
    private WorkoutWeek testWorkoutWeek;
    private WorkoutWeekRequest validWeekRequest;
    private UUID testPlanId;
    private UUID testWeekId;
    private Coach testCoach;
    private User coachUser;

    @BeforeEach
    void setUp() {
        testPlanId = UUID.randomUUID();
        testWeekId = UUID.randomUUID();

        coachUser = User.builder()
                .userId(UUID.randomUUID())
                .username("coach")
                .email("coach@example.com")
                .firstName("Coach")
                .lastName("User")
                .build();

        testCoach = Coach.builder()
                .coachId(UUID.randomUUID())
                .app_user(coachUser)
                .bio("Experienced coach")
                .build();

        testWorkoutPlan = WorkoutPlan.builder()
                .planId(testPlanId)
                .name("Test Plan")
                .description("Test Description")
                .totalWeeks(12)
                .coach(testCoach)
                .isActive(true)
                .isTemplate(false)
                .createdAt(LocalDateTime.now())
                .weeks(new ArrayList<>())
                .build();

        testWorkoutWeek = WorkoutWeek.builder()
                .weekId(testWeekId)
                .weekNumber(1)
                .notes("Week 1 notes")
                .workoutPlan(testWorkoutPlan)
                .days(new ArrayList<>())
                .build();

        validWeekRequest = WorkoutWeekRequest.builder()
                .weekNumber(1)
                .notes("Week 1 notes")
                .workoutPlanId(testPlanId)
                .build();
    }

    @Test
    @DisplayName("Should create workout week successfully")
    void createWorkoutWeek_WithValidRequest_ShouldCreateWeek() {
        // Given
        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutWeekRepository.existsByWorkoutPlan_PlanIdAndWeekNumber(testPlanId, 1)).willReturn(false);
        given(workoutWeekRepository.save(any(WorkoutWeek.class))).willReturn(testWorkoutWeek);

        // When
        WorkoutWeekResponse result = workoutWeekService.createWorkoutWeek(validWeekRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWeekId()).isEqualTo(testWeekId);
        assertThat(result.getWeekNumber()).isEqualTo(1);
        assertThat(result.getNotes()).isEqualTo("Week 1 notes");
        assertThat(result.getDayCount()).isEqualTo(0);

        then(workoutWeekRepository).should().save(any(WorkoutWeek.class));
    }

    @Test
    @DisplayName("Should throw exception when workout plan not found")
    void createWorkoutWeek_WithInvalidPlanId_ShouldThrowWorkoutPlanNotFoundException() {
        // Given
        UUID invalidPlanId = UUID.randomUUID();
        validWeekRequest.setWorkoutPlanId(invalidPlanId);

        given(workoutPlanRepository.findById(invalidPlanId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutWeekService.createWorkoutWeek(validWeekRequest))
                .isInstanceOf(WorkoutPlanNotFoundException.class)
                .hasMessageContaining(invalidPlanId.toString());

        then(workoutWeekRepository).should(never()).save(any(WorkoutWeek.class));
    }

    @Test
    @DisplayName("Should throw exception when workout plan is inactive")
    void createWorkoutWeek_WithInactivePlan_ShouldThrowWorkoutPlanInactiveException() {
        // Given
        testWorkoutPlan.setIsActive(false);
        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));

        // When & Then
        assertThatThrownBy(() -> workoutWeekService.createWorkoutWeek(validWeekRequest))
                .isInstanceOf(WorkoutPlanInactiveException.class)
                .hasMessageContaining(testPlanId.toString());

        then(workoutWeekRepository).should(never()).save(any(WorkoutWeek.class));
    }

    @Test
    @DisplayName("Should throw exception when week number already exists")
    void createWorkoutWeek_WithDuplicateWeekNumber_ShouldThrowDuplicateWorkoutStructureException() {
        // Given
        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutWeekRepository.existsByWorkoutPlan_PlanIdAndWeekNumber(testPlanId, 1)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> workoutWeekService.createWorkoutWeek(validWeekRequest))
                .isInstanceOf(DuplicateWorkoutStructureException.class)
                .hasMessageContaining("Week 1 already exists");

        then(workoutWeekRepository).should(never()).save(any(WorkoutWeek.class));
    }

    @Test
    @DisplayName("Should throw exception when week number exceeds plan total weeks")
    void createWorkoutWeek_WithWeekNumberExceedingTotal_ShouldThrowInvalidWorkoutStructureException() {
        // Given
        validWeekRequest.setWeekNumber(15); // Plan has only 12 weeks

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutWeekRepository.existsByWorkoutPlan_PlanIdAndWeekNumber(testPlanId, 15)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> workoutWeekService.createWorkoutWeek(validWeekRequest))
                .isInstanceOf(InvalidWorkoutStructureException.class)
                .hasMessageContaining("exceeds plan total weeks");

        then(workoutWeekRepository).should(never()).save(any(WorkoutWeek.class));
    }

    @Test
    @DisplayName("Should get workout week by ID successfully")
    void getWorkoutWeekById_WithValidId_ShouldReturnWeek() {
        // Given
        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));

        // When
        WorkoutWeekResponse result = workoutWeekService.getWorkoutWeekById(testWeekId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWeekId()).isEqualTo(testWeekId);
        assertThat(result.getWeekNumber()).isEqualTo(1);
        assertThat(result.getNotes()).isEqualTo("Week 1 notes");
    }

    @Test
    @DisplayName("Should throw exception when workout week not found")
    void getWorkoutWeekById_WithInvalidId_ShouldThrowWorkoutWeekNotFoundException() {
        // Given
        UUID invalidWeekId = UUID.randomUUID();
        given(workoutWeekRepository.findById(invalidWeekId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutWeekService.getWorkoutWeekById(invalidWeekId))
                .isInstanceOf(WorkoutWeekNotFoundException.class)
                .hasMessageContaining(invalidWeekId.toString());
    }

    @Test
    @DisplayName("Should get workout weeks by plan successfully")
    void getWorkoutWeeksByPlan_WithValidPlanId_ShouldReturnWeeks() {
        // Given
        WorkoutWeek week2 = WorkoutWeek.builder()
                .weekId(UUID.randomUUID())
                .weekNumber(2)
                .notes("Week 2 notes")
                .workoutPlan(testWorkoutPlan)
                .days(new ArrayList<>())
                .build();

        List<WorkoutWeek> weeks = Arrays.asList(testWorkoutWeek, week2);

        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutWeekRepository.findByWorkoutPlan_PlanIdOrderByWeekNumber(testPlanId)).willReturn(weeks);

        // When
        List<WorkoutWeekSummaryResponse> result = workoutWeekService.getWorkoutWeeksByPlan(testPlanId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getWeekNumber()).isEqualTo(1);
        assertThat(result.get(1).getWeekNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get workout week by plan and number successfully")
    void getWorkoutWeekByPlanAndNumber_WithValidPlanAndNumber_ShouldReturnWeek() {
        // Given
        given(workoutWeekRepository.findByWorkoutPlan_PlanIdAndWeekNumber(testPlanId, 1))
                .willReturn(Optional.of(testWorkoutWeek));

        // When
        WorkoutWeekResponse result = workoutWeekService.getWorkoutWeekByPlanAndNumber(testPlanId, 1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWeekNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw exception when week not found by plan and number")
    void getWorkoutWeekByPlanAndNumber_WithInvalidWeekNumber_ShouldThrowWorkoutWeekNotFoundException() {
        // Given
        given(workoutWeekRepository.findByWorkoutPlan_PlanIdAndWeekNumber(testPlanId, 99))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutWeekService.getWorkoutWeekByPlanAndNumber(testPlanId, 99))
                .isInstanceOf(WorkoutWeekNotFoundException.class)
                .hasMessageContaining("Week 99 not found in workout plan");
    }

    @Test
    @DisplayName("Should update workout week successfully")
    void updateWorkoutWeek_WithValidRequest_ShouldUpdateWeek() {
        // Given
        WorkoutWeekRequest updateRequest = WorkoutWeekRequest.builder()
                .weekNumber(2)
                .notes("Updated notes")
                .workoutPlanId(testPlanId)
                .build();

        WorkoutWeek updatedWeek = WorkoutWeek.builder()
                .weekId(testWeekId)
                .weekNumber(2)
                .notes("Updated notes")
                .workoutPlan(testWorkoutPlan)
                .days(new ArrayList<>())
                .build();

        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));
        given(workoutWeekRepository.existsByWorkoutPlan_PlanIdAndWeekNumber(testPlanId, 2)).willReturn(false);
        given(workoutWeekRepository.save(any(WorkoutWeek.class))).willReturn(updatedWeek);

        // When
        WorkoutWeekResponse result = workoutWeekService.updateWorkoutWeek(testWeekId, updateRequest);

        // Then
        assertThat(result.getWeekNumber()).isEqualTo(2);
        assertThat(result.getNotes()).isEqualTo("Updated notes");
    }

    @Test
    @DisplayName("Should throw exception when updating week number to existing one")
    void updateWorkoutWeek_WithDuplicateWeekNumber_ShouldThrowDuplicateWorkoutStructureException() {
        // Given
        WorkoutWeekRequest updateRequest = WorkoutWeekRequest.builder()
                .weekNumber(3) // Different from current (1)
                .notes("Updated notes")
                .workoutPlanId(testPlanId)
                .build();

        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));
        given(workoutWeekRepository.existsByWorkoutPlan_PlanIdAndWeekNumber(testPlanId, 3)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> workoutWeekService.updateWorkoutWeek(testWeekId, updateRequest))
                .isInstanceOf(DuplicateWorkoutStructureException.class)
                .hasMessageContaining("Week 3 already exists");
    }

    @Test
    @DisplayName("Should throw exception when updating week number exceeds plan total")
    void updateWorkoutWeek_WithWeekNumberExceedingTotal_ShouldThrowInvalidWorkoutStructureException() {
        // Given
        WorkoutWeekRequest updateRequest = WorkoutWeekRequest.builder()
                .weekNumber(15) // Plan has only 12 weeks
                .notes("Updated notes")
                .workoutPlanId(testPlanId)
                .build();

        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));
        given(workoutWeekRepository.existsByWorkoutPlan_PlanIdAndWeekNumber(testPlanId, 15)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> workoutWeekService.updateWorkoutWeek(testWeekId, updateRequest))
                .isInstanceOf(InvalidWorkoutStructureException.class)
                .hasMessageContaining("exceeds plan total weeks");
    }

    @Test
    @DisplayName("Should allow updating to same week number")
    void updateWorkoutWeek_WithSameWeekNumber_ShouldNotCheckDuplicate() {
        // Given
        WorkoutWeekRequest updateRequest = WorkoutWeekRequest.builder()
                .weekNumber(1) // Same as current
                .notes("Updated notes")
                .workoutPlanId(testPlanId)
                .build();

        WorkoutWeek updatedWeek = WorkoutWeek.builder()
                .weekId(testWeekId)
                .weekNumber(1)
                .notes("Updated notes")
                .workoutPlan(testWorkoutPlan)
                .days(new ArrayList<>())
                .build();

        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));
        given(workoutWeekRepository.save(any(WorkoutWeek.class))).willReturn(updatedWeek);

        // When
        WorkoutWeekResponse result = workoutWeekService.updateWorkoutWeek(testWeekId, updateRequest);

        // Then
        assertThat(result.getNotes()).isEqualTo("Updated notes");
        then(workoutWeekRepository).should(never()).existsByWorkoutPlan_PlanIdAndWeekNumber(any(UUID.class), any(Integer.class));
    }

    @Test
    @DisplayName("Should delete workout week successfully")
    void deleteWorkoutWeek_WithValidId_ShouldDeleteWeek() {
        // Given
        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));

        // When
        workoutWeekService.deleteWorkoutWeek(testWeekId);

        // Then
        then(workoutWeekRepository).should().delete(testWorkoutWeek);
    }

    @Test
    @DisplayName("Should throw exception when deleting week from inactive plan")
    void deleteWorkoutWeek_WithInactivePlan_ShouldThrowWorkoutPlanInactiveException() {
        // Given
        testWorkoutPlan.setIsActive(false);
        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));

        // When & Then
        assertThatThrownBy(() -> workoutWeekService.deleteWorkoutWeek(testWeekId))
                .isInstanceOf(WorkoutPlanInactiveException.class);

        then(workoutWeekRepository).should(never()).delete(any(WorkoutWeek.class));
    }

    @Test
    @DisplayName("Should get next week number successfully")
    void getNextWeekNumber_WithExistingWeeks_ShouldReturnNextNumber() {
        // Given
        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutWeekRepository.findMaxWeekNumberByPlanId(testPlanId)).willReturn(Optional.of(3));

        // When
        Integer result = workoutWeekService.getNextWeekNumber(testPlanId);

        // Then
        assertThat(result).isEqualTo(4);
    }

    @Test
    @DisplayName("Should return 1 for first week when no weeks exist")
    void getNextWeekNumber_WithNoExistingWeeks_ShouldReturnOne() {
        // Given
        given(workoutPlanRepository.findById(testPlanId)).willReturn(Optional.of(testWorkoutPlan));
        given(workoutWeekRepository.findMaxWeekNumberByPlanId(testPlanId)).willReturn(Optional.empty());

        // When
        Integer result = workoutWeekService.getNextWeekNumber(testPlanId);

        // Then
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("Should calculate day completion correctly")
    void mapToResponse_WithDaysAndSets_ShouldCalculateCompletion() {
        // Given
        WorkoutDay day1 = WorkoutDay.builder()
                .dayId(UUID.randomUUID())
                .dayNumber(1)
                .name("Day 1")
                .workoutWeek(testWorkoutWeek)
                .exercises(new ArrayList<>())
                .build();

        Exercise exercise1 = Exercise.builder()
                .exerciseId(UUID.randomUUID())
                .name("Squat")
                .exerciseOrder(1)
                .workoutDay(day1)
                .sets(new ArrayList<>())
                .build();

        WorkoutSet set1 = WorkoutSet.builder()
                .setId(UUID.randomUUID())
                .setNumber(1)
                .targetReps(5)
                .isCompleted(true)
                .exercise(exercise1)
                .build();

        WorkoutSet set2 = WorkoutSet.builder()
                .setId(UUID.randomUUID())
                .setNumber(2)
                .targetReps(5)
                .isCompleted(false)
                .exercise(exercise1)
                .build();

        exercise1.getSets().add(set1);
        exercise1.getSets().add(set2);
        day1.getExercises().add(exercise1);
        testWorkoutWeek.getDays().add(day1);

        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));

        // When
        WorkoutWeekResponse result = workoutWeekService.getWorkoutWeekById(testWeekId);

        // Then
        assertThat(result.getDays()).hasSize(1);
        assertThat(result.getDays().get(0).getIsCompleted()).isFalse(); // Not all sets completed
        assertThat(result.getDays().get(0).getExerciseCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle empty week correctly")
    void mapToResponse_WithEmptyWeek_ShouldReturnZeroCounts() {
        // Given
        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));

        // When
        WorkoutWeekResponse result = workoutWeekService.getWorkoutWeekById(testWeekId);

        // Then
        assertThat(result.getDays()).isEmpty();
        assertThat(result.getDayCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should update only provided fields")
    void updateWorkoutWeek_WithPartialRequest_ShouldUpdateOnlyProvidedFields() {
        // Given
        WorkoutWeekRequest partialRequest = WorkoutWeekRequest.builder()
                .notes("Only notes updated")
                .workoutPlanId(testPlanId)
                .build();

        WorkoutWeek updatedWeek = WorkoutWeek.builder()
                .weekId(testWeekId)
                .weekNumber(1) // Unchanged
                .notes("Only notes updated") // Changed
                .workoutPlan(testWorkoutPlan)
                .days(new ArrayList<>())
                .build();

        given(workoutWeekRepository.findById(testWeekId)).willReturn(Optional.of(testWorkoutWeek));
        given(workoutWeekRepository.save(any(WorkoutWeek.class))).willReturn(updatedWeek);

        // When
        WorkoutWeekResponse result = workoutWeekService.updateWorkoutWeek(testWeekId, partialRequest);

        // Then
        assertThat(result.getWeekNumber()).isEqualTo(1); // Unchanged
        assertThat(result.getNotes()).isEqualTo("Only notes updated"); // Changed
    }
}
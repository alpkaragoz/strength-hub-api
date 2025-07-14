package com.strengthhub.strength_hub_api.service.Workout;

import com.strengthhub.strength_hub_api.dto.request.workout.ExerciseRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.ExerciseResponse;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutSetResponse;
import com.strengthhub.strength_hub_api.exception.workout.*;
import com.strengthhub.strength_hub_api.model.workout.WorkoutDay;
import com.strengthhub.strength_hub_api.model.workout.Exercise;
import com.strengthhub.strength_hub_api.model.workout.WorkoutSet;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutDayRepository;
import com.strengthhub.strength_hub_api.repository.workout.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutDayRepository workoutDayRepository;

    @Transactional
    public ExerciseResponse createExercise(ExerciseRequest request) {
        log.info("Creating exercise {} for day {}", request.getName(), request.getDayId());

        WorkoutDay day = workoutDayRepository.findById(request.getDayId())
                .orElseThrow(() -> new WorkoutDayNotFoundException(request.getDayId()));

        if (!day.getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(day.getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        // Check if exercise order already exists in this day
        if (exerciseRepository.existsByWorkoutDay_DayIdAndExerciseOrder(
                request.getDayId(), request.getExerciseOrder())) {
            throw new DuplicateWorkoutStructureException("Exercise", request.getExerciseOrder(), request.getDayId());
        }

        Exercise exercise = Exercise.builder()
                .name(request.getName())
                .exerciseOrder(request.getExerciseOrder())
                .notes(request.getNotes())
                .workoutDay(day)
                .build();

        Exercise savedExercise = exerciseRepository.save(exercise);
        log.info("Exercise created with id: {}", savedExercise.getExerciseId());

        return mapToResponse(savedExercise);
    }

    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(UUID exerciseId) {
        log.info("Fetching exercise with id: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        return mapToResponse(exercise);
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByDay(UUID dayId) {
        log.info("Fetching exercises for day: {}", dayId);

        WorkoutDay day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new WorkoutDayNotFoundException(dayId));

        return exerciseRepository.findByWorkoutDay_DayIdOrderByExerciseOrder(dayId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByPlan(UUID planId) {
        log.info("Fetching all exercises for plan: {}", planId);

        return exerciseRepository.findByPlanIdOrderByWeekDayAndOrder(planId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> searchExercisesByDayAndName(UUID dayId, String searchTerm) {
        log.info("Searching exercises in day {} with term: {}", dayId, searchTerm);

        WorkoutDay day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new WorkoutDayNotFoundException(dayId));

        return exerciseRepository.findByDayIdAndNameContaining(dayId, searchTerm)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExerciseResponse updateExercise(UUID exerciseId, ExerciseRequest request) {
        log.info("Updating exercise with id: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        if (!exercise.getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(exercise.getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        // Update exercise order if changed
        if (request.getExerciseOrder() != null && !request.getExerciseOrder().equals(exercise.getExerciseOrder())) {
            // Check if new exercise order already exists in this day
            if (exerciseRepository.existsByWorkoutDay_DayIdAndExerciseOrder(
                    exercise.getWorkoutDay().getDayId(), request.getExerciseOrder())) {
                throw new DuplicateWorkoutStructureException("Exercise", request.getExerciseOrder(), exercise.getWorkoutDay().getDayId());
            }

            exercise.setExerciseOrder(request.getExerciseOrder());
        }

        if (request.getName() != null) {
            exercise.setName(request.getName());
        }

        if (request.getNotes() != null) {
            exercise.setNotes(request.getNotes());
        }

        Exercise updatedExercise = exerciseRepository.save(exercise);
        log.info("Exercise updated with id: {}", exerciseId);

        return mapToResponse(updatedExercise);
    }

    @Transactional
    public void deleteExercise(UUID exerciseId) {
        log.info("Deleting exercise with id: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        if (!exercise.getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(exercise.getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        exerciseRepository.delete(exercise);
        log.info("Exercise deleted with id: {}", exerciseId);
    }

    @Transactional
    public ExerciseResponse reorderExercise(UUID exerciseId, Integer newOrder) {
        log.info("Reordering exercise {} to position {}", exerciseId, newOrder);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        if (!exercise.getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(exercise.getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        if (newOrder <= 0) {
            throw new InvalidWorkoutStructureException("reorder exercise", "Exercise order must be positive");
        }

        UUID dayId = exercise.getWorkoutDay().getDayId();
        Integer currentOrder = exercise.getExerciseOrder();

        if (currentOrder.equals(newOrder)) {
            return mapToResponse(exercise);
        }

        // Get all exercises in the day
        List<Exercise> dayExercises = exerciseRepository.findByWorkoutDay_DayIdOrderByExerciseOrder(dayId);

        // Check if new order position exists
        if (newOrder > dayExercises.size()) {
            throw new InvalidWorkoutStructureException("reorder exercise",
                    "New order " + newOrder + " exceeds total exercises " + dayExercises.size());
        }

        // Reorder exercises
        if (currentOrder < newOrder) {
            // Moving down - shift exercises up
            for (Exercise e : dayExercises) {
                if (e.getExerciseOrder() > currentOrder && e.getExerciseOrder() <= newOrder) {
                    e.setExerciseOrder(e.getExerciseOrder() - 1);
                }
            }
        } else {
            // Moving up - shift exercises down
            for (Exercise e : dayExercises) {
                if (e.getExerciseOrder() >= newOrder && e.getExerciseOrder() < currentOrder) {
                    e.setExerciseOrder(e.getExerciseOrder() + 1);
                }
            }
        }

        exercise.setExerciseOrder(newOrder);
        exerciseRepository.saveAll(dayExercises);

        log.info("Exercise {} reordered to position {}", exerciseId, newOrder);
        return mapToResponse(exercise);
    }

    @Transactional(readOnly = true)
    public Integer getNextExerciseOrder(UUID dayId) {
        log.info("Getting next exercise order for day: {}", dayId);

        WorkoutDay day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new WorkoutDayNotFoundException(dayId));

        return exerciseRepository.findMaxExerciseOrderByDayId(dayId)
                .map(maxOrder -> maxOrder + 1)
                .orElse(1);
    }

    @Transactional(readOnly = true)
    public Boolean isExerciseCompleted(UUID exerciseId) {
        log.info("Checking completion status for exercise: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        return calculateExerciseCompletion(exercise);
    }

    private ExerciseResponse mapToResponse(Exercise exercise) {
        List<WorkoutSetResponse> setResponses = exercise.getSets().stream()
                .map(this::mapToSetResponse)
                .collect(Collectors.toList());

        return ExerciseResponse.builder()
                .exerciseId(exercise.getExerciseId())
                .name(exercise.getName())
                .exerciseOrder(exercise.getExerciseOrder())
                .notes(exercise.getNotes())
                .sets(setResponses)
                .setCount(exercise.getSetCount())
                .isCompleted(calculateExerciseCompletion(exercise))
                .build();
    }

    private WorkoutSetResponse mapToSetResponse(WorkoutSet set) {
        return WorkoutSetResponse.builder()
                .setId(set.getSetId())
                .setNumber(set.getSetNumber())
                .targetReps(set.getTargetReps())
                .targetWeight(set.getTargetWeight())
                .targetRpe(set.getTargetRpe())
                .actualReps(set.getActualReps())
                .actualWeight(set.getActualWeight())
                .actualRpe(set.getActualRpe())
                .lifterNotes(set.getLifterNotes())
                .isCompleted(set.getIsCompleted())
                .build();
    }

    private Boolean calculateExerciseCompletion(Exercise exercise) {
        if (exercise.getSets().isEmpty()) {
            return false;
        }

        return exercise.getSets().stream()
                .allMatch(set -> set.getIsCompleted());
    }
}

package com.strengthhub.strength_hub_api.service.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutSetRequest;
import com.strengthhub.strength_hub_api.dto.request.workout.SetCompletionRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutSetResponse;
import com.strengthhub.strength_hub_api.exception.workout.*;
import com.strengthhub.strength_hub_api.model.workout.Exercise;
import com.strengthhub.strength_hub_api.model.workout.WorkoutSet;
import com.strengthhub.strength_hub_api.repository.workout.ExerciseRepository;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutSetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutSetService {

    private final WorkoutSetRepository workoutSetRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    public WorkoutSetResponse createWorkoutSet(WorkoutSetRequest request) {
        log.info("Creating workout set {} for exercise {}", request.getSetNumber(), request.getExerciseId());

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ExerciseNotFoundException(request.getExerciseId()));

        if (!exercise.getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(exercise.getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        // Check if set number already exists in this exercise
        if (workoutSetRepository.existsByExercise_ExerciseIdAndSetNumber(
                request.getExerciseId(), request.getSetNumber())) {
            throw new DuplicateWorkoutStructureException("Set", request.getSetNumber(), request.getExerciseId());
        }

        WorkoutSet workoutSet = WorkoutSet.builder()
                .setNumber(request.getSetNumber())
                .targetReps(request.getTargetReps())
                .targetWeight(request.getTargetWeight())
                .targetRpe(request.getTargetRpe())
                .exercise(exercise)
                .isCompleted(false)
                .build();

        WorkoutSet savedSet = workoutSetRepository.save(workoutSet);
        log.info("Workout set created with id: {}", savedSet.getSetId());

        return mapToResponse(savedSet);
    }

    @Transactional(readOnly = true)
    public WorkoutSetResponse getWorkoutSetById(UUID setId) {
        log.info("Fetching workout set with id: {}", setId);

        WorkoutSet workoutSet = workoutSetRepository.findById(setId)
                .orElseThrow(() -> new WorkoutSetNotFoundException(setId));

        return mapToResponse(workoutSet);
    }

    @Transactional(readOnly = true)
    public List<WorkoutSetResponse> getWorkoutSetsByExercise(UUID exerciseId) {
        log.info("Fetching workout sets for exercise: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        return workoutSetRepository.findByExercise_ExerciseIdOrderBySetNumber(exerciseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkoutSetResponse getWorkoutSetByExerciseAndNumber(UUID exerciseId, Integer setNumber) {
        log.info("Fetching set {} for exercise {}", setNumber, exerciseId);

        WorkoutSet workoutSet = workoutSetRepository.findByExercise_ExerciseIdAndSetNumber(exerciseId, setNumber)
                .orElseThrow(() -> new WorkoutSetNotFoundException(exerciseId, setNumber));

        return mapToResponse(workoutSet);
    }

    @Transactional(readOnly = true)
    public List<WorkoutSetResponse> getCompletedSetsByExercise(UUID exerciseId) {
        log.info("Fetching completed sets for exercise: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        return workoutSetRepository.findByExercise_ExerciseIdAndIsCompletedOrderBySetNumber(exerciseId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkoutSetResponse> getSetsWithNotesByExercise(UUID exerciseId) {
        log.info("Fetching sets with notes for exercise: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        return workoutSetRepository.findByExerciseIdWithNotes(exerciseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkoutSetResponse updateWorkoutSet(UUID setId, WorkoutSetRequest request) {
        log.info("Updating workout set with id: {}", setId);

        WorkoutSet workoutSet = workoutSetRepository.findById(setId)
                .orElseThrow(() -> new WorkoutSetNotFoundException(setId));

        if (!workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        // Update set number if changed
        if (request.getSetNumber() != null && !request.getSetNumber().equals(workoutSet.getSetNumber())) {
            // Check if new set number already exists in this exercise
            if (workoutSetRepository.existsByExercise_ExerciseIdAndSetNumber(
                    workoutSet.getExercise().getExerciseId(), request.getSetNumber())) {
                throw new DuplicateWorkoutStructureException("Set", request.getSetNumber(), workoutSet.getExercise().getExerciseId());
            }

            workoutSet.setSetNumber(request.getSetNumber());
        }

        if (request.getTargetReps() != null) {
            workoutSet.setTargetReps(request.getTargetReps());
        }

        if (request.getTargetWeight() != null) {
            workoutSet.setTargetWeight(request.getTargetWeight());
        }

        if (request.getTargetRpe() != null) {
            workoutSet.setTargetRpe(request.getTargetRpe());
        }

        WorkoutSet updatedSet = workoutSetRepository.save(workoutSet);
        log.info("Workout set updated with id: {}", setId);

        return mapToResponse(updatedSet);
    }

    @Transactional
    public WorkoutSetResponse completeWorkoutSet(UUID setId, SetCompletionRequest request) {
        log.info("Completing workout set with id: {}", setId);

        WorkoutSet workoutSet = workoutSetRepository.findById(setId)
                .orElseThrow(() -> new WorkoutSetNotFoundException(setId));

        if (!workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        workoutSet.setActualReps(request.getActualReps());
        workoutSet.setActualWeight(request.getActualWeight());
        workoutSet.setActualRpe(request.getActualRpe());
        workoutSet.setLifterNotes(request.getLifterNotes());
        workoutSet.setIsCompleted(true);

        WorkoutSet completedSet = workoutSetRepository.save(workoutSet);
        log.info("Workout set completed with id: {}", setId);

        return mapToResponse(completedSet);
    }

    @Transactional
    public WorkoutSetResponse uncompleteWorkoutSet(UUID setId) {
        log.info("Uncompleting workout set with id: {}", setId);

        WorkoutSet workoutSet = workoutSetRepository.findById(setId)
                .orElseThrow(() -> new WorkoutSetNotFoundException(setId));

        if (!workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        workoutSet.setActualReps(null);
        workoutSet.setActualWeight(null);
        workoutSet.setActualRpe(null);
        workoutSet.setLifterNotes(null);
        workoutSet.setIsCompleted(false);

        WorkoutSet uncompletedSet = workoutSetRepository.save(workoutSet);
        log.info("Workout set uncompleted with id: {}", setId);

        return mapToResponse(uncompletedSet);
    }

    @Transactional
    public void deleteWorkoutSet(UUID setId) {
        log.info("Deleting workout set with id: {}", setId);

        WorkoutSet workoutSet = workoutSetRepository.findById(setId)
                .orElseThrow(() -> new WorkoutSetNotFoundException(setId));

        if (!workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        workoutSetRepository.delete(workoutSet);
        log.info("Workout set deleted with id: {}", setId);
    }

    @Transactional
    public WorkoutSetResponse reorderWorkoutSet(UUID setId, Integer newSetNumber) {
        log.info("Reordering workout set {} to position {}", setId, newSetNumber);

        WorkoutSet workoutSet = workoutSetRepository.findById(setId)
                .orElseThrow(() -> new WorkoutSetNotFoundException(setId));

        if (!workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(workoutSet.getExercise().getWorkoutDay().getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        if (newSetNumber <= 0) {
            throw new InvalidWorkoutStructureException("reorder set", "Set number must be positive");
        }

        UUID exerciseId = workoutSet.getExercise().getExerciseId();
        Integer currentSetNumber = workoutSet.getSetNumber();

        if (currentSetNumber.equals(newSetNumber)) {
            return mapToResponse(workoutSet);
        }

        // Get all sets in the exercise
        List<WorkoutSet> exerciseSets = workoutSetRepository.findByExercise_ExerciseIdOrderBySetNumber(exerciseId);

        // Check if new set number position exists
        if (newSetNumber > exerciseSets.size()) {
            throw new InvalidWorkoutStructureException("reorder set",
                    "New set number " + newSetNumber + " exceeds total sets " + exerciseSets.size());
        }

        // Reorder sets
        if (currentSetNumber < newSetNumber) {
            // Moving down - shift sets up
            for (WorkoutSet s : exerciseSets) {
                if (s.getSetNumber() > currentSetNumber && s.getSetNumber() <= newSetNumber) {
                    s.setSetNumber(s.getSetNumber() - 1);
                }
            }
        } else {
            // Moving up - shift sets down
            for (WorkoutSet s : exerciseSets) {
                if (s.getSetNumber() >= newSetNumber && s.getSetNumber() < currentSetNumber) {
                    s.setSetNumber(s.getSetNumber() + 1);
                }
            }
        }

        workoutSet.setSetNumber(newSetNumber);
        workoutSetRepository.saveAll(exerciseSets);

        log.info("Workout set {} reordered to position {}", setId, newSetNumber);
        return mapToResponse(workoutSet);
    }

    @Transactional(readOnly = true)
    public Integer getNextSetNumber(UUID exerciseId) {
        log.info("Getting next set number for exercise: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        return workoutSetRepository.findMaxSetNumberByExerciseId(exerciseId)
                .map(maxSetNumber -> maxSetNumber + 1)
                .orElse(1);
    }

    @Transactional(readOnly = true)
    public Long getCompletedSetCount(UUID exerciseId) {
        log.info("Getting completed set count for exercise: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        return workoutSetRepository.countByExercise_ExerciseIdAndIsCompleted(exerciseId, true);
    }

    @Transactional(readOnly = true)
    public Long getTotalSetCount(UUID exerciseId) {
        log.info("Getting total set count for exercise: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        return workoutSetRepository.countByExercise_ExerciseId(exerciseId);
    }

    private WorkoutSetResponse mapToResponse(WorkoutSet workoutSet) {
        return WorkoutSetResponse.builder()
                .setId(workoutSet.getSetId())
                .setNumber(workoutSet.getSetNumber())
                .targetReps(workoutSet.getTargetReps())
                .targetWeight(workoutSet.getTargetWeight())
                .targetRpe(workoutSet.getTargetRpe())
                .actualReps(workoutSet.getActualReps())
                .actualWeight(workoutSet.getActualWeight())
                .actualRpe(workoutSet.getActualRpe())
                .lifterNotes(workoutSet.getLifterNotes())
                .isCompleted(workoutSet.getIsCompleted())
                .build();
    }
}

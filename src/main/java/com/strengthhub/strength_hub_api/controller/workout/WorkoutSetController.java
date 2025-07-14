package com.strengthhub.strength_hub_api.controller.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutSetRequest;
import com.strengthhub.strength_hub_api.dto.request.workout.SetCompletionRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutSetResponse;
import com.strengthhub.strength_hub_api.service.workout.WorkoutSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workout-sets")
@RequiredArgsConstructor
@Validated
public class WorkoutSetController {

    private final WorkoutSetService workoutSetService;

    @PostMapping
    public ResponseEntity<WorkoutSetResponse> createWorkoutSet(@Valid @RequestBody WorkoutSetRequest request) {
        WorkoutSetResponse createdSet = workoutSetService.createWorkoutSet(request);
        return new ResponseEntity<>(createdSet, HttpStatus.CREATED);
    }

    @GetMapping("/{setId}")
    public ResponseEntity<WorkoutSetResponse> getWorkoutSetById(@PathVariable UUID setId) {
        WorkoutSetResponse workoutSet = workoutSetService.getWorkoutSetById(setId);
        return ResponseEntity.ok(workoutSet);
    }

    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<List<WorkoutSetResponse>> getWorkoutSetsByExercise(@PathVariable UUID exerciseId) {
        List<WorkoutSetResponse> sets = workoutSetService.getWorkoutSetsByExercise(exerciseId);
        return ResponseEntity.ok(sets);
    }

    @GetMapping("/exercise/{exerciseId}/set/{setNumber}")
    public ResponseEntity<WorkoutSetResponse> getWorkoutSetByExerciseAndNumber(@PathVariable UUID exerciseId,
                                                                               @PathVariable Integer setNumber) {
        WorkoutSetResponse workoutSet = workoutSetService.getWorkoutSetByExerciseAndNumber(exerciseId, setNumber);
        return ResponseEntity.ok(workoutSet);
    }

    @GetMapping("/exercise/{exerciseId}/completed")
    public ResponseEntity<List<WorkoutSetResponse>> getCompletedSetsByExercise(@PathVariable UUID exerciseId) {
        List<WorkoutSetResponse> completedSets = workoutSetService.getCompletedSetsByExercise(exerciseId);
        return ResponseEntity.ok(completedSets);
    }

    @GetMapping("/exercise/{exerciseId}/with-notes")
    public ResponseEntity<List<WorkoutSetResponse>> getSetsWithNotesByExercise(@PathVariable UUID exerciseId) {
        List<WorkoutSetResponse> setsWithNotes = workoutSetService.getSetsWithNotesByExercise(exerciseId);
        return ResponseEntity.ok(setsWithNotes);
    }

    @PutMapping("/{setId}")
    public ResponseEntity<WorkoutSetResponse> updateWorkoutSet(@PathVariable UUID setId,
                                                               @Valid @RequestBody WorkoutSetRequest request) {
        WorkoutSetResponse updatedSet = workoutSetService.updateWorkoutSet(setId, request);
        return ResponseEntity.ok(updatedSet);
    }

    @PutMapping("/{setId}/complete")
    public ResponseEntity<WorkoutSetResponse> completeWorkoutSet(@PathVariable UUID setId,
                                                                 @Valid @RequestBody SetCompletionRequest request) {
        WorkoutSetResponse completedSet = workoutSetService.completeWorkoutSet(setId, request);
        return ResponseEntity.ok(completedSet);
    }

    @PutMapping("/{setId}/uncomplete")
    public ResponseEntity<WorkoutSetResponse> uncompleteWorkoutSet(@PathVariable UUID setId) {
        WorkoutSetResponse uncompletedSet = workoutSetService.uncompleteWorkoutSet(setId);
        return ResponseEntity.ok(uncompletedSet);
    }

    @PutMapping("/{setId}/reorder")
    public ResponseEntity<WorkoutSetResponse> reorderWorkoutSet(@PathVariable UUID setId,
                                                                @RequestParam Integer newSetNumber) {
        WorkoutSetResponse reorderedSet = workoutSetService.reorderWorkoutSet(setId, newSetNumber);
        return ResponseEntity.ok(reorderedSet);
    }

    @DeleteMapping("/{setId}")
    public ResponseEntity<Void> deleteWorkoutSet(@PathVariable UUID setId) {
        workoutSetService.deleteWorkoutSet(setId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exercise/{exerciseId}/next-set-number")
    public ResponseEntity<Integer> getNextSetNumber(@PathVariable UUID exerciseId) {
        Integer nextSetNumber = workoutSetService.getNextSetNumber(exerciseId);
        return ResponseEntity.ok(nextSetNumber);
    }

    @GetMapping("/exercise/{exerciseId}/completed-count")
    public ResponseEntity<Long> getCompletedSetCount(@PathVariable UUID exerciseId) {
        Long completedCount = workoutSetService.getCompletedSetCount(exerciseId);
        return ResponseEntity.ok(completedCount);
    }

    @GetMapping("/exercise/{exerciseId}/total-count")
    public ResponseEntity<Long> getTotalSetCount(@PathVariable UUID exerciseId) {
        Long totalCount = workoutSetService.getTotalSetCount(exerciseId);
        return ResponseEntity.ok(totalCount);
    }
}

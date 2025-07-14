package com.strengthhub.strength_hub_api.controller.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.ExerciseRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.ExerciseResponse;
import com.strengthhub.strength_hub_api.service.workout.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
@Validated
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(@Valid @RequestBody ExerciseRequest request) {
        ExerciseResponse createdExercise = exerciseService.createExercise(request);
        return new ResponseEntity<>(createdExercise, HttpStatus.CREATED);
    }

    @GetMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> getExerciseById(@PathVariable UUID exerciseId) {
        ExerciseResponse exercise = exerciseService.getExerciseById(exerciseId);
        return ResponseEntity.ok(exercise);
    }

    @GetMapping("/day/{dayId}")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByDay(@PathVariable UUID dayId) {
        List<ExerciseResponse> exercises = exerciseService.getExercisesByDay(dayId);
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByPlan(@PathVariable UUID planId) {
        List<ExerciseResponse> exercises = exerciseService.getExercisesByPlan(planId);
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/day/{dayId}/search")
    public ResponseEntity<List<ExerciseResponse>> searchExercisesByDayAndName(@PathVariable UUID dayId,
                                                                              @RequestParam String searchTerm) {
        List<ExerciseResponse> exercises = exerciseService.searchExercisesByDayAndName(dayId, searchTerm);
        return ResponseEntity.ok(exercises);
    }

    @PutMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> updateExercise(@PathVariable UUID exerciseId,
                                                           @Valid @RequestBody ExerciseRequest request) {
        ExerciseResponse updatedExercise = exerciseService.updateExercise(exerciseId, request);
        return ResponseEntity.ok(updatedExercise);
    }

    @PutMapping("/{exerciseId}/reorder")
    public ResponseEntity<ExerciseResponse> reorderExercise(@PathVariable UUID exerciseId,
                                                            @RequestParam Integer newOrder) {
        ExerciseResponse reorderedExercise = exerciseService.reorderExercise(exerciseId, newOrder);
        return ResponseEntity.ok(reorderedExercise);
    }

    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(@PathVariable UUID exerciseId) {
        exerciseService.deleteExercise(exerciseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/day/{dayId}/next-exercise-order")
    public ResponseEntity<Integer> getNextExerciseOrder(@PathVariable UUID dayId) {
        Integer nextExerciseOrder = exerciseService.getNextExerciseOrder(dayId);
        return ResponseEntity.ok(nextExerciseOrder);
    }

    @GetMapping("/{exerciseId}/completion-status")
    public ResponseEntity<Boolean> isExerciseCompleted(@PathVariable UUID exerciseId) {
        Boolean isCompleted = exerciseService.isExerciseCompleted(exerciseId);
        return ResponseEntity.ok(isCompleted);
    }
}

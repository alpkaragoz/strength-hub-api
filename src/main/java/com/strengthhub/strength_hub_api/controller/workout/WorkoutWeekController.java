package com.strengthhub.strength_hub_api.controller.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutWeekRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutWeekResponse;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutWeekSummaryResponse;
import com.strengthhub.strength_hub_api.service.workout.WorkoutWeekService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workout-weeks")
@RequiredArgsConstructor
@Validated
public class WorkoutWeekController {

    private final WorkoutWeekService workoutWeekService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<WorkoutWeekResponse> createWorkoutWeek(@Valid @RequestBody WorkoutWeekRequest request) {
        WorkoutWeekResponse createdWeek = workoutWeekService.createWorkoutWeek(request);
        return new ResponseEntity<>(createdWeek, HttpStatus.CREATED);
    }

    @GetMapping("/{weekId}")
    public ResponseEntity<WorkoutWeekResponse> getWorkoutWeekById(@PathVariable UUID weekId) {
        WorkoutWeekResponse week = workoutWeekService.getWorkoutWeekById(weekId);
        return ResponseEntity.ok(week);
    }

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<WorkoutWeekSummaryResponse>> getWorkoutWeeksByPlan(@PathVariable UUID planId) {
        List<WorkoutWeekSummaryResponse> weeks = workoutWeekService.getWorkoutWeeksByPlan(planId);
        return ResponseEntity.ok(weeks);
    }

    @GetMapping("/plan/{planId}/week/{weekNumber}")
    public ResponseEntity<WorkoutWeekResponse> getWorkoutWeekByPlanAndNumber(@PathVariable UUID planId,
                                                                             @PathVariable Integer weekNumber) {
        WorkoutWeekResponse week = workoutWeekService.getWorkoutWeekByPlanAndNumber(planId, weekNumber);
        return ResponseEntity.ok(week);
    }

    @PutMapping("/{weekId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<WorkoutWeekResponse> updateWorkoutWeek(@PathVariable UUID weekId,
                                                                 @Valid @RequestBody WorkoutWeekRequest request) {
        WorkoutWeekResponse updatedWeek = workoutWeekService.updateWorkoutWeek(weekId, request);
        return ResponseEntity.ok(updatedWeek);
    }

    @DeleteMapping("/{weekId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<Void> deleteWorkoutWeek(@PathVariable UUID weekId) {
        workoutWeekService.deleteWorkoutWeek(weekId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/plan/{planId}/next-week-number")
    public ResponseEntity<Integer> getNextWeekNumber(@PathVariable UUID planId) {
        Integer nextWeekNumber = workoutWeekService.getNextWeekNumber(planId);
        return ResponseEntity.ok(nextWeekNumber);
    }
}
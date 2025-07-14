package com.strengthhub.strength_hub_api.controller.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutPlanCreateRequest;
import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutPlanUpdateRequest;
import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutPlanAssignmentRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.*;
import com.strengthhub.strength_hub_api.service.workout.WorkoutPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workout-plans")
@RequiredArgsConstructor
@Validated
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @PostMapping
    public ResponseEntity<WorkoutPlanResponse> createWorkoutPlan(@Valid @RequestBody WorkoutPlanCreateRequest request) {
        WorkoutPlanResponse createdPlan = workoutPlanService.createWorkoutPlan(request);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<WorkoutPlanDetailResponse> getWorkoutPlanById(@PathVariable UUID planId) {
        WorkoutPlanDetailResponse plan = workoutPlanService.getWorkoutPlanById(planId);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/coach/{coachId}")
    public ResponseEntity<List<WorkoutPlanSummaryResponse>> getWorkoutPlansByCoach(@PathVariable UUID coachId) {
        List<WorkoutPlanSummaryResponse> plans = workoutPlanService.getWorkoutPlansByCoach(coachId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/lifter/{lifterId}")
    public ResponseEntity<List<WorkoutPlanSummaryResponse>> getWorkoutPlansByLifter(@PathVariable UUID lifterId) {
        List<WorkoutPlanSummaryResponse> plans = workoutPlanService.getWorkoutPlansByLifter(lifterId);
        return ResponseEntity.ok(plans);
    }

    @PutMapping("/{planId}")
    public ResponseEntity<WorkoutPlanResponse> updateWorkoutPlan(@PathVariable UUID planId,
                                                                 @Valid @RequestBody WorkoutPlanUpdateRequest request) {
        WorkoutPlanResponse updatedPlan = workoutPlanService.updateWorkoutPlan(planId, request);
        return ResponseEntity.ok(updatedPlan);
    }

    @PostMapping("/{planId}/assign-lifter")
    public ResponseEntity<Void> assignLifterToWorkoutPlan(@PathVariable UUID planId,
                                                          @Valid @RequestBody WorkoutPlanAssignmentRequest request) {
        workoutPlanService.assignLifterToWorkoutPlan(planId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{planId}/unassign-lifter")
    public ResponseEntity<Void> unassignLifterFromWorkoutPlan(@PathVariable UUID planId) {
        workoutPlanService.unassignLifterFromWorkoutPlan(planId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deleteWorkoutPlan(@PathVariable UUID planId) {
        workoutPlanService.deleteWorkoutPlan(planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{planId}/stats")
    public ResponseEntity<WorkoutStatsResponse> getWorkoutPlanStats(@PathVariable UUID planId) {
        WorkoutStatsResponse stats = workoutPlanService.getWorkoutPlanStats(planId);
        return ResponseEntity.ok(stats);
    }
}

package com.strengthhub.strength_hub_api.controller.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutDayRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutDayResponse;
import com.strengthhub.strength_hub_api.service.workout.WorkoutDayService;
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
@RequestMapping("/api/v1/workout-days")
@RequiredArgsConstructor
@Validated
public class WorkoutDayController {

    private final WorkoutDayService workoutDayService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<WorkoutDayResponse> createWorkoutDay(@Valid @RequestBody WorkoutDayRequest request) {
        WorkoutDayResponse createdDay = workoutDayService.createWorkoutDay(request);
        return new ResponseEntity<>(createdDay, HttpStatus.CREATED);
    }

    @GetMapping("/{dayId}")
    public ResponseEntity<WorkoutDayResponse> getWorkoutDayById(@PathVariable UUID dayId) {
        WorkoutDayResponse day = workoutDayService.getWorkoutDayById(dayId);
        return ResponseEntity.ok(day);
    }

    @GetMapping("/week/{weekId}")
    public ResponseEntity<List<WorkoutDayResponse>> getWorkoutDaysByWeek(@PathVariable UUID weekId) {
        List<WorkoutDayResponse> days = workoutDayService.getWorkoutDaysByWeek(weekId);
        return ResponseEntity.ok(days);
    }

    @GetMapping("/week/{weekId}/day/{dayNumber}")
    public ResponseEntity<WorkoutDayResponse> getWorkoutDayByWeekAndNumber(@PathVariable UUID weekId,
                                                                           @PathVariable Integer dayNumber) {
        WorkoutDayResponse day = workoutDayService.getWorkoutDayByWeekAndNumber(weekId, dayNumber);
        return ResponseEntity.ok(day);
    }

    @GetMapping("/plan/{planId}/week/{weekNumber}")
    public ResponseEntity<List<WorkoutDayResponse>> getWorkoutDaysByPlanAndWeek(@PathVariable UUID planId,
                                                                                @PathVariable Integer weekNumber) {
        List<WorkoutDayResponse> days = workoutDayService.getWorkoutDaysByPlanAndWeek(planId, weekNumber);
        return ResponseEntity.ok(days);
    }

    @PutMapping("/{dayId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<WorkoutDayResponse> updateWorkoutDay(@PathVariable UUID dayId,
                                                               @Valid @RequestBody WorkoutDayRequest request) {
        WorkoutDayResponse updatedDay = workoutDayService.updateWorkoutDay(dayId, request);
        return ResponseEntity.ok(updatedDay);
    }

    @DeleteMapping("/{dayId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<Void> deleteWorkoutDay(@PathVariable UUID dayId) {
        workoutDayService.deleteWorkoutDay(dayId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/week/{weekId}/next-day-number")
    public ResponseEntity<Integer> getNextDayNumber(@PathVariable UUID weekId) {
        Integer nextDayNumber = workoutDayService.getNextDayNumber(weekId);
        return ResponseEntity.ok(nextDayNumber);
    }

    @GetMapping("/{dayId}/completion-status")
    public ResponseEntity<Boolean> isDayCompleted(@PathVariable UUID dayId) {
        Boolean isCompleted = workoutDayService.isDayCompleted(dayId);
        return ResponseEntity.ok(isCompleted);
    }
}

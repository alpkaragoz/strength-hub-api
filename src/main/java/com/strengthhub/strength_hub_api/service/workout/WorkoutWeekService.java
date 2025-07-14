package com.strengthhub.strength_hub_api.service.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutWeekRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutWeekResponse;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutWeekSummaryResponse;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutDayResponse;
import com.strengthhub.strength_hub_api.exception.workout.*;
import com.strengthhub.strength_hub_api.model.workout.WorkoutPlan;
import com.strengthhub.strength_hub_api.model.workout.WorkoutWeek;
import com.strengthhub.strength_hub_api.model.workout.WorkoutDay;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutPlanRepository;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutWeekRepository;
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
public class WorkoutWeekService {

    private final WorkoutWeekRepository workoutWeekRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    @Transactional
    public WorkoutWeekResponse createWorkoutWeek(WorkoutWeekRequest request) {
        log.info("Creating workout week {} for plan {}", request.getWeekNumber(), request.getWorkoutPlanId());

        WorkoutPlan plan = workoutPlanRepository.findById(request.getWorkoutPlanId())
                .orElseThrow(() -> new WorkoutPlanNotFoundException(request.getWorkoutPlanId()));

        if (!plan.getIsActive()) {
            throw new WorkoutPlanInactiveException(plan.getPlanId());
        }

        // Check if week number already exists in this plan
        if (workoutWeekRepository.existsByWorkoutPlan_PlanIdAndWeekNumber(
                request.getWorkoutPlanId(), request.getWeekNumber())) {
            throw new DuplicateWorkoutStructureException("Week", request.getWeekNumber(), request.getWorkoutPlanId());
        }

        // Validate week number is within plan limits
        if (request.getWeekNumber() > plan.getTotalWeeks()) {
            throw new InvalidWorkoutStructureException(
                    "create week",
                    "Week number " + request.getWeekNumber() + " exceeds plan total weeks " + plan.getTotalWeeks()
            );
        }

        WorkoutWeek week = WorkoutWeek.builder()
                .weekNumber(request.getWeekNumber())
                .notes(request.getNotes())
                .workoutPlan(plan)
                .build();

        WorkoutWeek savedWeek = workoutWeekRepository.save(week);
        log.info("Workout week created with id: {}", savedWeek.getWeekId());

        return mapToResponse(savedWeek);
    }

    @Transactional(readOnly = true)
    public WorkoutWeekResponse getWorkoutWeekById(UUID weekId) {
        log.info("Fetching workout week with id: {}", weekId);

        WorkoutWeek week = workoutWeekRepository.findById(weekId)
                .orElseThrow(() -> new WorkoutWeekNotFoundException(weekId));

        return mapToResponse(week);
    }

    @Transactional(readOnly = true)
    public List<WorkoutWeekSummaryResponse> getWorkoutWeeksByPlan(UUID planId) {
        log.info("Fetching workout weeks for plan: {}", planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(planId));

        return workoutWeekRepository.findByWorkoutPlan_PlanIdOrderByWeekNumber(planId)
                .stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkoutWeekResponse getWorkoutWeekByPlanAndNumber(UUID planId, Integer weekNumber) {
        log.info("Fetching week {} for plan {}", weekNumber, planId);

        WorkoutWeek week = workoutWeekRepository.findByWorkoutPlan_PlanIdAndWeekNumber(planId, weekNumber)
                .orElseThrow(() -> new WorkoutWeekNotFoundException(planId, weekNumber));

        return mapToResponse(week);
    }

    @Transactional
    public WorkoutWeekResponse updateWorkoutWeek(UUID weekId, WorkoutWeekRequest request) {
        log.info("Updating workout week with id: {}", weekId);

        WorkoutWeek week = workoutWeekRepository.findById(weekId)
                .orElseThrow(() -> new WorkoutWeekNotFoundException(weekId));

        if (!week.getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(week.getWorkoutPlan().getPlanId());
        }

        // Update week number if changed
        if (request.getWeekNumber() != null && !request.getWeekNumber().equals(week.getWeekNumber())) {
            // Check if new week number already exists in this plan
            if (workoutWeekRepository.existsByWorkoutPlan_PlanIdAndWeekNumber(
                    week.getWorkoutPlan().getPlanId(), request.getWeekNumber())) {
                throw new DuplicateWorkoutStructureException("Week", request.getWeekNumber(), week.getWorkoutPlan().getPlanId());
            }

            // Validate new week number is within plan limits
            if (request.getWeekNumber() > week.getWorkoutPlan().getTotalWeeks()) {
                throw new InvalidWorkoutStructureException(
                        "update week",
                        "Week number " + request.getWeekNumber() + " exceeds plan total weeks " + week.getWorkoutPlan().getTotalWeeks()
                );
            }

            week.setWeekNumber(request.getWeekNumber());
        }

        if (request.getNotes() != null) {
            week.setNotes(request.getNotes());
        }

        WorkoutWeek updatedWeek = workoutWeekRepository.save(week);
        log.info("Workout week updated with id: {}", weekId);

        return mapToResponse(updatedWeek);
    }

    @Transactional
    public void deleteWorkoutWeek(UUID weekId) {
        log.info("Deleting workout week with id: {}", weekId);

        WorkoutWeek week = workoutWeekRepository.findById(weekId)
                .orElseThrow(() -> new WorkoutWeekNotFoundException(weekId));

        if (!week.getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(week.getWorkoutPlan().getPlanId());
        }

        workoutWeekRepository.delete(week);
        log.info("Workout week deleted with id: {}", weekId);
    }

    @Transactional(readOnly = true)
    public Integer getNextWeekNumber(UUID planId) {
        log.info("Getting next week number for plan: {}", planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(planId));

        return workoutWeekRepository.findMaxWeekNumberByPlanId(planId)
                .map(maxWeek -> maxWeek + 1)
                .orElse(1);
    }

    private WorkoutWeekResponse mapToResponse(WorkoutWeek week) {
        List<WorkoutDayResponse> dayResponses = week.getDays().stream()
                .map(this::mapToDayResponse)
                .collect(Collectors.toList());

        return WorkoutWeekResponse.builder()
                .weekId(week.getWeekId())
                .weekNumber(week.getWeekNumber())
                .notes(week.getNotes())
                .days(dayResponses)
                .dayCount(week.getDayCount())
                .build();
    }

    private WorkoutWeekSummaryResponse mapToSummaryResponse(WorkoutWeek week) {
        return WorkoutWeekSummaryResponse.builder()
                .weekId(week.getWeekId())
                .weekNumber(week.getWeekNumber())
                .notes(week.getNotes())
                .dayCount(week.getDayCount())
                .build();
    }

    private WorkoutDayResponse mapToDayResponse(WorkoutDay day) {
        return WorkoutDayResponse.builder()
                .dayId(day.getDayId())
                .dayNumber(day.getDayNumber())
                .name(day.getName())
                .notes(day.getNotes())
                .exerciseCount(day.getExerciseCount())
                .isCompleted(calculateDayCompletion(day))
                .build();
    }

    private Boolean calculateDayCompletion(WorkoutDay day) {
        if (day.getExercises().isEmpty()) {
            return false;
        }

        return day.getExercises().stream()
                .allMatch(exercise ->
                        !exercise.getSets().isEmpty() &&
                                exercise.getSets().stream().allMatch(set -> set.getIsCompleted())
                );
    }
}

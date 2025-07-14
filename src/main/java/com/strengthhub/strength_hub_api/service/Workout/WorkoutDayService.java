package com.strengthhub.strength_hub_api.service.Workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutDayRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.WorkoutDayResponse;
import com.strengthhub.strength_hub_api.dto.response.workout.ExerciseResponse;
import com.strengthhub.strength_hub_api.exception.workout.*;
import com.strengthhub.strength_hub_api.model.workout.WorkoutWeek;
import com.strengthhub.strength_hub_api.model.workout.WorkoutDay;
import com.strengthhub.strength_hub_api.model.workout.Exercise;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutWeekRepository;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutDayRepository;
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
public class WorkoutDayService {

    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutWeekRepository workoutWeekRepository;

    @Transactional
    public WorkoutDayResponse createWorkoutDay(WorkoutDayRequest request) {
        log.info("Creating workout day {} for week {}", request.getDayNumber(), request.getWeekId());

        WorkoutWeek week = workoutWeekRepository.findById(request.getWeekId())
                .orElseThrow(() -> new WorkoutWeekNotFoundException(request.getWeekId()));

        if (!week.getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(week.getWorkoutPlan().getPlanId());
        }

        // Check if day number already exists in this week
        if (workoutDayRepository.existsByWorkoutWeek_WeekIdAndDayNumber(
                request.getWeekId(), request.getDayNumber())) {
            throw new DuplicateWorkoutStructureException("Day", request.getDayNumber(), request.getWeekId());
        }

        WorkoutDay day = WorkoutDay.builder()
                .dayNumber(request.getDayNumber())
                .name(request.getName())
                .notes(request.getNotes())
                .workoutWeek(week)
                .build();

        WorkoutDay savedDay = workoutDayRepository.save(day);
        log.info("Workout day created with id: {}", savedDay.getDayId());

        return mapToResponse(savedDay);
    }

    @Transactional(readOnly = true)
    public WorkoutDayResponse getWorkoutDayById(UUID dayId) {
        log.info("Fetching workout day with id: {}", dayId);

        WorkoutDay day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new WorkoutDayNotFoundException(dayId));

        return mapToResponse(day);
    }

    @Transactional(readOnly = true)
    public List<WorkoutDayResponse> getWorkoutDaysByWeek(UUID weekId) {
        log.info("Fetching workout days for week: {}", weekId);

        WorkoutWeek week = workoutWeekRepository.findById(weekId)
                .orElseThrow(() -> new WorkoutWeekNotFoundException(weekId));

        return workoutDayRepository.findByWorkoutWeek_WeekIdOrderByDayNumber(weekId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkoutDayResponse getWorkoutDayByWeekAndNumber(UUID weekId, Integer dayNumber) {
        log.info("Fetching day {} for week {}", dayNumber, weekId);

        WorkoutDay day = workoutDayRepository.findByWorkoutWeek_WeekIdAndDayNumber(weekId, dayNumber)
                .orElseThrow(() -> new WorkoutDayNotFoundException(weekId, dayNumber));

        return mapToResponse(day);
    }

    @Transactional(readOnly = true)
    public List<WorkoutDayResponse> getWorkoutDaysByPlanAndWeek(UUID planId, Integer weekNumber) {
        log.info("Fetching workout days for plan {} week {}", planId, weekNumber);

        return workoutDayRepository.findByPlanIdAndWeekNumber(planId, weekNumber)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkoutDayResponse updateWorkoutDay(UUID dayId, WorkoutDayRequest request) {
        log.info("Updating workout day with id: {}", dayId);

        WorkoutDay day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new WorkoutDayNotFoundException(dayId));

        if (!day.getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(day.getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        // Update day number if changed
        if (request.getDayNumber() != null && !request.getDayNumber().equals(day.getDayNumber())) {
            // Check if new day number already exists in this week
            if (workoutDayRepository.existsByWorkoutWeek_WeekIdAndDayNumber(
                    day.getWorkoutWeek().getWeekId(), request.getDayNumber())) {
                throw new DuplicateWorkoutStructureException("Day", request.getDayNumber(), day.getWorkoutWeek().getWeekId());
            }

            day.setDayNumber(request.getDayNumber());
        }

        if (request.getName() != null) {
            day.setName(request.getName());
        }

        if (request.getNotes() != null) {
            day.setNotes(request.getNotes());
        }

        WorkoutDay updatedDay = workoutDayRepository.save(day);
        log.info("Workout day updated with id: {}", dayId);

        return mapToResponse(updatedDay);
    }

    @Transactional
    public void deleteWorkoutDay(UUID dayId) {
        log.info("Deleting workout day with id: {}", dayId);

        WorkoutDay day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new WorkoutDayNotFoundException(dayId));

        if (!day.getWorkoutWeek().getWorkoutPlan().getIsActive()) {
            throw new WorkoutPlanInactiveException(day.getWorkoutWeek().getWorkoutPlan().getPlanId());
        }

        workoutDayRepository.delete(day);
        log.info("Workout day deleted with id: {}", dayId);
    }

    @Transactional(readOnly = true)
    public Integer getNextDayNumber(UUID weekId) {
        log.info("Getting next day number for week: {}", weekId);

        WorkoutWeek week = workoutWeekRepository.findById(weekId)
                .orElseThrow(() -> new WorkoutWeekNotFoundException(weekId));

        return workoutDayRepository.findMaxDayNumberByWeekId(weekId)
                .map(maxDay -> maxDay + 1)
                .orElse(1);
    }

    @Transactional(readOnly = true)
    public Boolean isDayCompleted(UUID dayId) {
        log.info("Checking completion status for day: {}", dayId);

        WorkoutDay day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new WorkoutDayNotFoundException(dayId));

        return calculateDayCompletion(day);
    }

    private WorkoutDayResponse mapToResponse(WorkoutDay day) {
        List<ExerciseResponse> exerciseResponses = day.getExercises().stream()
                .map(this::mapToExerciseResponse)
                .collect(Collectors.toList());

        return WorkoutDayResponse.builder()
                .dayId(day.getDayId())
                .dayNumber(day.getDayNumber())
                .name(day.getName())
                .notes(day.getNotes())
                .exercises(exerciseResponses)
                .exerciseCount(day.getExerciseCount())
                .isCompleted(calculateDayCompletion(day))
                .build();
    }

    private ExerciseResponse mapToExerciseResponse(Exercise exercise) {
        return ExerciseResponse.builder()
                .exerciseId(exercise.getExerciseId())
                .name(exercise.getName())
                .exerciseOrder(exercise.getExerciseOrder())
                .notes(exercise.getNotes())
                .setCount(exercise.getSetCount())
                .isCompleted(calculateExerciseCompletion(exercise))
                .build();
    }

    private Boolean calculateDayCompletion(WorkoutDay day) {
        if (day.getExercises().isEmpty()) {
            return false;
        }

        return day.getExercises().stream()
                .allMatch(this::calculateExerciseCompletion);
    }

    private Boolean calculateExerciseCompletion(Exercise exercise) {
        if (exercise.getSets().isEmpty()) {
            return false;
        }

        return exercise.getSets().stream()
                .allMatch(set -> set.getIsCompleted());
    }
}
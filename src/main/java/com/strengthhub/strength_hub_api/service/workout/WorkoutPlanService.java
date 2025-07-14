package com.strengthhub.strength_hub_api.service.workout;

import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutPlanCreateRequest;
import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutPlanUpdateRequest;
import com.strengthhub.strength_hub_api.dto.request.workout.WorkoutPlanAssignmentRequest;
import com.strengthhub.strength_hub_api.dto.response.workout.*;
import com.strengthhub.strength_hub_api.dto.response.coach.CoachSummaryResponse;
import com.strengthhub.strength_hub_api.dto.response.lifter.LifterSummaryResponse;
import com.strengthhub.strength_hub_api.exception.coach.CoachNotFoundException;
import com.strengthhub.strength_hub_api.exception.lifter.LifterNotFoundException;
import com.strengthhub.strength_hub_api.exception.workout.*;
import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.Lifter;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.model.workout.WorkoutPlan;
import com.strengthhub.strength_hub_api.model.workout.WorkoutWeek;
import com.strengthhub.strength_hub_api.repository.CoachRepository;
import com.strengthhub.strength_hub_api.repository.LifterRepository;
import com.strengthhub.strength_hub_api.repository.workout.WorkoutPlanRepository;
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
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final CoachRepository coachRepository;
    private final LifterRepository lifterRepository;
    private final WorkoutSetRepository workoutSetRepository;

    @Transactional
    public WorkoutPlanResponse createWorkoutPlan(WorkoutPlanCreateRequest request) {
        log.info("Creating workout plan: {}", request.getName());

        // Validate coach exists
        Coach coach = coachRepository.findById(request.getCoachId())
                .orElseThrow(() -> new CoachNotFoundException(request.getCoachId()));

        // Check if lifter exists if assigned
        Lifter assignedLifter = null;
        if (request.getAssignedLifterId() != null) {
            assignedLifter = lifterRepository.findById(request.getAssignedLifterId())
                    .orElseThrow(() -> new LifterNotFoundException(request.getAssignedLifterId()));

            // Check if lifter already has an active plan
            if (workoutPlanRepository.findActiveByLifterId(request.getAssignedLifterId()).isPresent()) {
                throw new WorkoutPlanAlreadyAssignedException(request.getAssignedLifterId());
            }
        }

        WorkoutPlan workoutPlan = WorkoutPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .totalWeeks(request.getTotalWeeks())
                .coach(coach)
                .assignedLifter(assignedLifter)
                .isTemplate(request.getIsTemplate())
                .isActive(true)
                .build();

        WorkoutPlan savedPlan = workoutPlanRepository.save(workoutPlan);
        log.info("Workout plan created with id: {}", savedPlan.getPlanId());

        return mapToResponse(savedPlan);
    }

    @Transactional(readOnly = true)
    public WorkoutPlanDetailResponse getWorkoutPlanById(UUID planId) {
        log.info("Fetching workout plan with id: {}", planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(planId));

        return mapToDetailResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<WorkoutPlanSummaryResponse> getWorkoutPlansByCoach(UUID coachId) {
        log.info("Fetching workout plans for coach: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new CoachNotFoundException(coachId));

        return workoutPlanRepository.findByCoach_CoachId(coachId)
                .stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkoutPlanSummaryResponse> getWorkoutPlansByLifter(UUID lifterId) {
        log.info("Fetching workout plans for lifter: {}", lifterId);

        Lifter lifter = lifterRepository.findById(lifterId)
                .orElseThrow(() -> new LifterNotFoundException(lifterId));

        return workoutPlanRepository.findByAssignedLifter_LifterId(lifterId)
                .stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkoutPlanResponse updateWorkoutPlan(UUID planId, WorkoutPlanUpdateRequest request) {
        log.info("Updating workout plan with id: {}", planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(planId));

        if (request.getName() != null) {
            plan.setName(request.getName());
        }

        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }

        if (request.getIsTemplate() != null) {
            plan.setIsTemplate(request.getIsTemplate());
        }

        if (request.getAssignedLifterId() != null) {
            Lifter lifter = lifterRepository.findById(request.getAssignedLifterId())
                    .orElseThrow(() -> new LifterNotFoundException(request.getAssignedLifterId()));

            // Check if lifter already has an active plan (excluding current plan)
            workoutPlanRepository.findActiveByLifterId(request.getAssignedLifterId())
                    .filter(existingPlan -> !existingPlan.getPlanId().equals(planId))
                    .ifPresent(existingPlan -> {
                        throw new WorkoutPlanAlreadyAssignedException(request.getAssignedLifterId());
                    });

            plan.setAssignedLifter(lifter);
        }

        WorkoutPlan updatedPlan = workoutPlanRepository.save(plan);
        log.info("Workout plan updated with id: {}", planId);

        return mapToResponse(updatedPlan);
    }

    @Transactional
    public void assignLifterToWorkoutPlan(UUID planId, WorkoutPlanAssignmentRequest request) {
        log.info("Assigning lifter {} to workout plan {}", request.getLifterId(), planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(planId));

        if (plan.getAssignedLifter() != null) {
            throw new WorkoutPlanAlreadyAssignedException(planId, plan.getAssignedLifter().getLifterId());
        }

        Lifter lifter = lifterRepository.findById(request.getLifterId())
                .orElseThrow(() -> new LifterNotFoundException(request.getLifterId()));

        // Check if lifter already has an active plan
        if (workoutPlanRepository.findActiveByLifterId(request.getLifterId()).isPresent()) {
            throw new WorkoutPlanAlreadyAssignedException(request.getLifterId());
        }

        plan.setAssignedLifter(lifter);
        workoutPlanRepository.save(plan);

        log.info("Lifter {} assigned to workout plan {}", request.getLifterId(), planId);
    }

    @Transactional
    public void unassignLifterFromWorkoutPlan(UUID planId) {
        log.info("Unassigning lifter from workout plan {}", planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(planId));

        if (plan.getAssignedLifter() == null) {
            throw new InvalidWorkoutStructureException("unassign lifter", "No lifter is currently assigned to this plan");
        }

        plan.setAssignedLifter(null);
        workoutPlanRepository.save(plan);

        log.info("Lifter unassigned from workout plan {}", planId);
    }

    @Transactional
    public void deleteWorkoutPlan(UUID planId) {
        log.info("Deleting workout plan with id: {}", planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(planId));

        workoutPlanRepository.delete(plan);
        log.info("Workout plan deleted with id: {}", planId);
    }

    @Transactional(readOnly = true)
    public WorkoutStatsResponse getWorkoutPlanStats(UUID planId) {
        log.info("Fetching stats for workout plan: {}", planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(planId));

        Long totalSets = workoutSetRepository.countByPlanId(planId);
        Long completedSets = workoutSetRepository.countCompletedByPlanId(planId);

        double completionPercentage = totalSets > 0 ? (completedSets.doubleValue() / totalSets.doubleValue()) * 100 : 0.0;

        return WorkoutStatsResponse.builder()
                .totalWeeks(plan.getTotalWeeks())
                .totalDays(calculateTotalDays(plan))
                .totalExercises(calculateTotalExercises(plan))
                .totalSets(totalSets.intValue())
                .completedSets(completedSets.intValue())
                .completionPercentage(completionPercentage)
                .build();
    }

    private Integer calculateTotalDays(WorkoutPlan plan) {
        return plan.getWeeks().stream()
                .mapToInt(week -> week.getDays().size())
                .sum();
    }

    private Integer calculateTotalExercises(WorkoutPlan plan) {
        return plan.getWeeks().stream()
                .flatMap(week -> week.getDays().stream())
                .mapToInt(day -> day.getExercises().size())
                .sum();
    }

    private WorkoutPlanResponse mapToResponse(WorkoutPlan plan) {
        return WorkoutPlanResponse.builder()
                .planId(plan.getPlanId())
                .name(plan.getName())
                .description(plan.getDescription())
                .totalWeeks(plan.getTotalWeeks())
                .isActive(plan.getIsActive())
                .isTemplate(plan.getIsTemplate())
                .createdAt(plan.getCreatedAt())
                .coach(mapToCoachSummary(plan.getCoach()))
                .assignedLifter(plan.getAssignedLifter() != null ? mapToLifterSummary(plan.getAssignedLifter()) : null)
                .weeks(plan.getWeeks().stream()
                        .map(this::mapToWeekSummary)
                        .collect(Collectors.toList()))
                .totalDays(calculateTotalDays(plan))
                .totalExercises(calculateTotalExercises(plan))
                .build();
    }

    private WorkoutPlanDetailResponse mapToDetailResponse(WorkoutPlan plan) {
        return WorkoutPlanDetailResponse.builder()
                .planId(plan.getPlanId())
                .name(plan.getName())
                .description(plan.getDescription())
                .totalWeeks(plan.getTotalWeeks())
                .isActive(plan.getIsActive())
                .isTemplate(plan.getIsTemplate())
                .createdAt(plan.getCreatedAt())
                .coach(mapToCoachSummary(plan.getCoach()))
                .assignedLifter(plan.getAssignedLifter() != null ? mapToLifterSummary(plan.getAssignedLifter()) : null)
                .stats(getWorkoutPlanStats(plan.getPlanId()))
                .build();
    }

    private WorkoutPlanSummaryResponse mapToSummaryResponse(WorkoutPlan plan) {
        return WorkoutPlanSummaryResponse.builder()
                .planId(plan.getPlanId())
                .name(plan.getName())
                .description(plan.getDescription())
                .totalWeeks(plan.getTotalWeeks())
                .isActive(plan.getIsActive())
                .isTemplate(plan.getIsTemplate())
                .createdAt(plan.getCreatedAt())
                .coach(mapToCoachSummary(plan.getCoach()))
                .assignedLifter(plan.getAssignedLifter() != null ? mapToLifterSummary(plan.getAssignedLifter()) : null)
                .build();
    }

    private WorkoutWeekSummaryResponse mapToWeekSummary(WorkoutWeek week) {
        return WorkoutWeekSummaryResponse.builder()
                .weekId(week.getWeekId())
                .weekNumber(week.getWeekNumber())
                .notes(week.getNotes())
                .dayCount(week.getDayCount())
                .build();
    }

    private CoachSummaryResponse mapToCoachSummary(Coach coach) {
        User user = coach.getApp_user();
        return CoachSummaryResponse.builder()
                .coachId(coach.getCoachId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .build();
    }

    private LifterSummaryResponse mapToLifterSummary(Lifter lifter) {
        User user = lifter.getApp_user();
        return LifterSummaryResponse.builder()
                .lifterId(lifter.getLifterId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .build();
    }
}

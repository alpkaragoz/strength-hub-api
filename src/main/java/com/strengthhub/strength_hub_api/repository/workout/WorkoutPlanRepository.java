package com.strengthhub.strength_hub_api.repository.workout;

import com.strengthhub.strength_hub_api.model.workout.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, UUID> {

    // Find plans by coach
    List<WorkoutPlan> findByCoach_CoachId(UUID coachId);

    // Find plans by assigned lifter
    List<WorkoutPlan> findByAssignedLifter_LifterId(UUID lifterId);

    // Find active plans by coach
    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.coach.coachId = :coachId AND wp.isActive = true")
    List<WorkoutPlan> findActiveByCoachId(@Param("coachId") UUID coachId);

    // Find active plan for a lifter
    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.assignedLifter.lifterId = :lifterId AND wp.isActive = true")
    Optional<WorkoutPlan> findActiveByLifterId(@Param("lifterId") UUID lifterId);

    // Find templates by coach
    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.coach.coachId = :coachId AND wp.isTemplate = true")
    List<WorkoutPlan> findTemplatesByCoachId(@Param("coachId") UUID coachId);

    // Find unassigned plans by coach
    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.coach.coachId = :coachId AND wp.assignedLifter IS NULL")
    List<WorkoutPlan> findUnassignedByCoachId(@Param("coachId") UUID coachId);

    // Search plans by name
    @Query("SELECT wp FROM WorkoutPlan wp WHERE " +
            "wp.coach.coachId = :coachId AND " +
            "LOWER(wp.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<WorkoutPlan> findByCoachIdAndNameContaining(@Param("coachId") UUID coachId,
                                                     @Param("search") String search);

    // Count total plans by coach
    Long countByCoach_CoachId(UUID coachId);

    // Count active plans by coach
    @Query("SELECT COUNT(wp) FROM WorkoutPlan wp WHERE wp.coach.coachId = :coachId AND wp.isActive = true")
    Long countActiveByCoachId(@Param("coachId") UUID coachId);
}

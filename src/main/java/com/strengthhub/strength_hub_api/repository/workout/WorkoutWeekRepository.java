package com.strengthhub.strength_hub_api.repository.workout;

import com.strengthhub.strength_hub_api.model.workout.WorkoutWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutWeekRepository extends JpaRepository<WorkoutWeek, UUID> {

    // Find weeks by workout plan
    List<WorkoutWeek> findByWorkoutPlan_PlanIdOrderByWeekNumber(UUID planId);

    // Find specific week in a plan
    Optional<WorkoutWeek> findByWorkoutPlan_PlanIdAndWeekNumber(UUID planId, Integer weekNumber);

    // Check if week number exists in plan
    boolean existsByWorkoutPlan_PlanIdAndWeekNumber(UUID planId, Integer weekNumber);

    // Find weeks with notes
    @Query("SELECT ww FROM WorkoutWeek ww WHERE ww.workoutPlan.planId = :planId AND ww.notes IS NOT NULL")
    List<WorkoutWeek> findByPlanIdWithNotes(@Param("planId") UUID planId);

    // Count weeks in a plan
    Long countByWorkoutPlan_PlanId(UUID planId);

    // Get maximum week number for a plan
    @Query("SELECT MAX(ww.weekNumber) FROM WorkoutWeek ww WHERE ww.workoutPlan.planId = :planId")
    Optional<Integer> findMaxWeekNumberByPlanId(@Param("planId") UUID planId);
}

package com.strengthhub.strength_hub_api.repository.workout;

import com.strengthhub.strength_hub_api.model.workout.WorkoutDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, UUID> {

    // Find days by workout week
    List<WorkoutDay> findByWorkoutWeek_WeekIdOrderByDayNumber(UUID weekId);

    // Find specific day in a week
    Optional<WorkoutDay> findByWorkoutWeek_WeekIdAndDayNumber(UUID weekId, Integer dayNumber);

    // Check if day number exists in week
    boolean existsByWorkoutWeek_WeekIdAndDayNumber(UUID weekId, Integer dayNumber);

    // Find days by plan and week number
    @Query("SELECT wd FROM WorkoutDay wd WHERE " +
            "wd.workoutWeek.workoutPlan.planId = :planId AND " +
            "wd.workoutWeek.weekNumber = :weekNumber " +
            "ORDER BY wd.dayNumber")
    List<WorkoutDay> findByPlanIdAndWeekNumber(@Param("planId") UUID planId,
                                               @Param("weekNumber") Integer weekNumber);

    // Find all days in a workout plan
    @Query("SELECT wd FROM WorkoutDay wd WHERE wd.workoutWeek.workoutPlan.planId = :planId " +
            "ORDER BY wd.workoutWeek.weekNumber, wd.dayNumber")
    List<WorkoutDay> findByPlanIdOrderByWeekAndDay(@Param("planId") UUID planId);

    // Count days in a week
    Long countByWorkoutWeek_WeekId(UUID weekId);

    // Count total days in a plan
    @Query("SELECT COUNT(wd) FROM WorkoutDay wd WHERE wd.workoutWeek.workoutPlan.planId = :planId")
    Long countByPlanId(@Param("planId") UUID planId);

    // Get maximum day number for a week
    @Query("SELECT MAX(wd.dayNumber) FROM WorkoutDay wd WHERE wd.workoutWeek.weekId = :weekId")
    Optional<Integer> findMaxDayNumberByWeekId(@Param("weekId") UUID weekId);
}

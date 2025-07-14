package com.strengthhub.strength_hub_api.repository.workout;

import com.strengthhub.strength_hub_api.model.workout.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    // Find exercises by workout day
    List<Exercise> findByWorkoutDay_DayIdOrderByExerciseOrder(UUID dayId);

    // Check if exercise order exists in day
    boolean existsByWorkoutDay_DayIdAndExerciseOrder(UUID dayId, Integer exerciseOrder);

    // Find exercises by name pattern in a day
    @Query("SELECT e FROM Exercise e WHERE " +
            "e.workoutDay.dayId = :dayId AND " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY e.exerciseOrder")
    List<Exercise> findByDayIdAndNameContaining(@Param("dayId") UUID dayId,
                                                @Param("search") String search);

    // Find all exercises in a workout plan
    @Query("SELECT e FROM Exercise e WHERE e.workoutDay.workoutWeek.workoutPlan.planId = :planId " +
            "ORDER BY e.workoutDay.workoutWeek.weekNumber, e.workoutDay.dayNumber, e.exerciseOrder")
    List<Exercise> findByPlanIdOrderByWeekDayAndOrder(@Param("planId") UUID planId);


    // Count exercises in a day
    Long countByWorkoutDay_DayId(UUID dayId);

    // Get maximum exercise order for a day
    @Query("SELECT MAX(e.exerciseOrder) FROM Exercise e WHERE e.workoutDay.dayId = :dayId")
    Optional<Integer> findMaxExerciseOrderByDayId(@Param("dayId") UUID dayId);
}

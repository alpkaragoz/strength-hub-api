package com.strengthhub.strength_hub_api.repository.workout;

import com.strengthhub.strength_hub_api.model.workout.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, UUID> {

    // Find sets by exercise
    List<WorkoutSet> findByExercise_ExerciseIdOrderBySetNumber(UUID exerciseId);

    // Find specific set in exercise
    Optional<WorkoutSet> findByExercise_ExerciseIdAndSetNumber(UUID exerciseId, Integer setNumber);

    // Check if set number exists in exercise
    boolean existsByExercise_ExerciseIdAndSetNumber(UUID exerciseId, Integer setNumber);

    // Find completed sets in exercise
    List<WorkoutSet> findByExercise_ExerciseIdAndIsCompletedOrderBySetNumber(UUID exerciseId, Boolean isCompleted);

    // Find sets with lifter notes
    @Query("SELECT ws FROM WorkoutSet ws WHERE " +
            "ws.exercise.exerciseId = :exerciseId AND " +
            "ws.lifterNotes IS NOT NULL AND " +
            "ws.lifterNotes != '' " +
            "ORDER BY ws.setNumber")
    List<WorkoutSet> findByExerciseIdWithNotes(@Param("exerciseId") UUID exerciseId);

    // Find all sets in a workout plan
    @Query("SELECT ws FROM WorkoutSet ws WHERE " +
            "ws.exercise.workoutDay.workoutWeek.workoutPlan.planId = :planId " +
            "ORDER BY ws.exercise.workoutDay.workoutWeek.weekNumber, " +
            "ws.exercise.workoutDay.dayNumber, " +
            "ws.exercise.exerciseOrder, " +
            "ws.setNumber")
    List<WorkoutSet> findByPlanIdOrderByWeekDayExerciseAndSet(@Param("planId") UUID planId);

    // Count sets in exercise
    Long countByExercise_ExerciseId(UUID exerciseId);

    // Count completed sets in exercise
    Long countByExercise_ExerciseIdAndIsCompleted(UUID exerciseId, Boolean isCompleted);

    // Count total sets in a workout plan
    @Query("SELECT COUNT(ws) FROM WorkoutSet ws WHERE " +
            "ws.exercise.workoutDay.workoutWeek.workoutPlan.planId = :planId")
    Long countByPlanId(@Param("planId") UUID planId);

    // Count completed sets in a workout plan
    @Query("SELECT COUNT(ws) FROM WorkoutSet ws WHERE " +
            "ws.exercise.workoutDay.workoutWeek.workoutPlan.planId = :planId AND " +
            "ws.isCompleted = true")
    Long countCompletedByPlanId(@Param("planId") UUID planId);

    // Get maximum set number for an exercise
    @Query("SELECT MAX(ws.setNumber) FROM WorkoutSet ws WHERE ws.exercise.exerciseId = :exerciseId")
    Optional<Integer> findMaxSetNumberByExerciseId(@Param("exerciseId") UUID exerciseId);
}
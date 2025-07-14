package com.strengthhub.strength_hub_api.model.workout;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Entity
@Table(name = "workout_day")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutDay {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID dayId;

    @Min(value = 1, message = "Day number must be at least 1")
    @Max(value = 7, message = "Day number cannot exceed 7")
    @Column(nullable = false)
    private Integer dayNumber;

    @Size(max = 100, message = "Day name cannot exceed 100 characters")
    @Column(nullable = false)
    private String name; // e.g., "Upper Body", "Squat Day", etc.

    @Size(max = 500, message = "Day notes cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    @ToString.Exclude
    private WorkoutWeek workoutWeek;

    @OneToMany(mappedBy = "workoutDay",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Exercise> exercises = new ArrayList<>();

    public void addExercise(Exercise exercise) {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        exercises.add(exercise);
        exercise.setWorkoutDay(this);
    }

    public void removeExercise(Exercise exercise) {
        if (exercises != null) {
            exercises.remove(exercise);
            exercise.setWorkoutDay(null);
        }
    }

    public int getExerciseCount() {
        return exercises != null ? exercises.size() : 0;
    }
}
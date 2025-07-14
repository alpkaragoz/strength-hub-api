package com.strengthhub.strength_hub_api.model.workout;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Entity
@Table(name = "workout_week")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID weekId;

    @Min(value = 1, message = "Week number must be at least 1")
    @Max(value = 16, message = "Week number cannot exceed 16")
    @Column(nullable = false)
    private Integer weekNumber;

    @Size(max = 200, message = "Week notes cannot exceed 200 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @ToString.Exclude
    private WorkoutPlan workoutPlan;

    @OneToMany(mappedBy = "workoutWeek",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<WorkoutDay> days = new ArrayList<>();

    public void addDay(WorkoutDay day) {
        if (days == null) {
            days = new ArrayList<>();
        }
        days.add(day);
        day.setWorkoutWeek(this);
    }

    public void removeDay(WorkoutDay day) {
        if (days != null) {
            days.remove(day);
            day.setWorkoutWeek(null);
        }
    }

    public int getDayCount() {
        return days != null ? days.size() : 0;
    }
}

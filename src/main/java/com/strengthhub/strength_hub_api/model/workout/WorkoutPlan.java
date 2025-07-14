package com.strengthhub.strength_hub_api.model.workout;

import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.Lifter;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "workout_plan")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID planId;

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name cannot exceed 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(value = 2, message = "Plan must be at least 2 weeks")
    @Max(value = 16, message = "Plan cannot exceed 16 weeks")
    @Column(nullable = false)
    private Integer totalWeeks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    @ToString.Exclude
    private Coach coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lifter_id", nullable = true)
    @ToString.Exclude
    private Lifter assignedLifter;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isTemplate = false; // Templates can be reused

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "workoutPlan",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<WorkoutWeek> weeks = new ArrayList<>();

    public void addWeek(WorkoutWeek week) {
        if (weeks == null) {
            weeks = new ArrayList<>();
        }
        weeks.add(week);
        week.setWorkoutPlan(this);
    }

    public void removeWeek(WorkoutWeek week) {
        if (weeks != null) {
            weeks.remove(week);
            week.setWorkoutPlan(null);
        }
    }
}
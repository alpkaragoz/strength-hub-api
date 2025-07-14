package com.strengthhub.strength_hub_api.model.workout;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Entity
@Table(name = "exercise")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID exerciseId;

    @NotBlank(message = "Exercise name is required")
    @Size(max = 100, message = "Exercise name cannot exceed 100 characters")
    @Column(nullable = false)
    private String name;

    @Min(value = 1, message = "Exercise order must be at least 1")
    @Column(nullable = false)
    private Integer exerciseOrder; // Order within the day

    @Size(max = 500, message = "Exercise notes cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    @ToString.Exclude
    private WorkoutDay workoutDay;

    @OneToMany(mappedBy = "exercise",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<WorkoutSet> sets = new ArrayList<>();

    public void addSet(WorkoutSet set) {
        if (sets == null) {
            sets = new ArrayList<>();
        }
        sets.add(set);
        set.setExercise(this);
    }

    public void removeSet(WorkoutSet set) {
        if (sets != null) {
            sets.remove(set);
            set.setExercise(null);
        }
    }

    public int getSetCount() {
        return sets != null ? sets.size() : 0;
    }
}

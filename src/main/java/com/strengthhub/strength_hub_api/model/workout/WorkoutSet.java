package com.strengthhub.strength_hub_api.model.workout;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workout_set")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID setId;

    @Min(value = 1, message = "Set number must be at least 1")
    @Column(nullable = false)
    private Integer setNumber;

    // Target values (set by coach)
    @Min(value = 1, message = "Target reps must be at least 1")
    @Column(nullable = false)
    private Integer targetReps;

    @DecimalMin(value = "0.0", message = "Target weight cannot be negative")
    @Column(precision = 6, scale = 2)
    private BigDecimal targetWeight;

    @DecimalMin(value = "6.0", message = "Target RPE must be at least 6.0")
    @DecimalMax(value = "10.0", message = "Target RPE cannot exceed 10.0")
    @Column(precision = 3, scale = 1)
    private BigDecimal targetRpe;

    // Actual values (recorded by lifter)
    @Min(value = 0, message = "Actual reps cannot be negative")
    private Integer actualReps;

    @DecimalMin(value = "0.0", message = "Actual weight cannot be negative")
    @Column(precision = 6, scale = 2)
    private BigDecimal actualWeight;

    @DecimalMin(value = "6.0", message = "Actual RPE must be at least 6.0")
    @DecimalMax(value = "10.0", message = "Actual RPE cannot exceed 10.0")
    @Column(precision = 3, scale = 1)
    private BigDecimal actualRpe;

    @Size(max = 300, message = "Lifter notes cannot exceed 300 characters")
    @Column(columnDefinition = "TEXT")
    private String lifterNotes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @ToString.Exclude
    private Exercise exercise;

    public boolean isActualDataComplete() {
        return actualReps != null && actualWeight != null && actualRpe != null;
    }
}

package com.strengthhub.strength_hub_api.dto.request.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSetRequest {

    @NotNull(message = "Set number is required")
    @Min(value = 1, message = "Set number must be at least 1")
    private Integer setNumber;

    @NotNull(message = "Target reps is required")
    @Min(value = 1, message = "Target reps must be at least 1")
    private Integer targetReps;

    @DecimalMin(value = "0.0", message = "Target weight cannot be negative")
    private BigDecimal targetWeight;

    @DecimalMin(value = "6.0", message = "Target RPE must be at least 6.0")
    @DecimalMax(value = "10.0", message = "Target RPE cannot exceed 10.0")
    private BigDecimal targetRpe;

    @NotNull(message = "Exercise ID is required")
    private UUID exerciseId;
}

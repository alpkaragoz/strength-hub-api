package com.strengthhub.strength_hub_api.dto.request.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutWeekRequest {

    @NotNull(message = "Week number is required")
    @Min(value = 1, message = "Week number must be at least 1")
    @Max(value = 16, message = "Week number cannot exceed 16")
    private Integer weekNumber;

    @Size(max = 200, message = "Week notes cannot exceed 200 characters")
    private String notes;

    @NotNull(message = "Workout plan ID is required")
    private UUID workoutPlanId;
}

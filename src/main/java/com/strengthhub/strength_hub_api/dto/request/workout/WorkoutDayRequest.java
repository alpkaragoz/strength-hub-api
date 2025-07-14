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
public class WorkoutDayRequest {

    @NotNull(message = "Day number is required")
    @Min(value = 1, message = "Day number must be at least 1")
    @Max(value = 7, message = "Day number cannot exceed 7")
    private Integer dayNumber;

    @NotBlank(message = "Day name is required")
    @Size(max = 100, message = "Day name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Day notes cannot exceed 500 characters")
    private String notes;

    @NotNull(message = "Week ID is required")
    private UUID weekId;
}

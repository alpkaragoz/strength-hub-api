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
public class WorkoutPlanCreateRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name cannot exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Total weeks is required")
    @Min(value = 2, message = "Plan must be at least 2 weeks")
    @Max(value = 16, message = "Plan cannot exceed 16 weeks")
    private Integer totalWeeks;

    @NotNull(message = "Coach ID is required")
    private UUID coachId;

    private UUID assignedLifterId; // Optional - can be assigned later

    @Builder.Default
    private Boolean isTemplate = false;
}

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
public class ExerciseRequest {

    @NotBlank(message = "Exercise name is required")
    @Size(max = 100, message = "Exercise name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Exercise order is required")
    @Min(value = 1, message = "Exercise order must be at least 1")
    private Integer exerciseOrder;

    @Size(max = 500, message = "Exercise notes cannot exceed 500 characters")
    private String notes;

    @NotNull(message = "Day ID is required")
    private UUID dayId;
}


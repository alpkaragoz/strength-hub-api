package com.strengthhub.strength_hub_api.dto.request.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanAssignmentRequest {

    @NotNull(message = "Lifter ID is required")
    private UUID lifterId;
}

package com.strengthhub.strength_hub_api.dto.response.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseResponse {
    private UUID exerciseId;
    private String name;
    private Integer exerciseOrder;
    private String notes;
    private List<WorkoutSetResponse> sets;
    private Integer setCount;
    private Boolean isCompleted; // All sets completed
}

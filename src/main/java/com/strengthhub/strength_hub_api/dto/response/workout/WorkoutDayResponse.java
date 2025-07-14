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
public class WorkoutDayResponse {
    private UUID dayId;
    private Integer dayNumber;
    private String name;
    private String notes;
    private List<ExerciseResponse> exercises;
    private Integer exerciseCount;
    private Boolean isCompleted; // All sets completed
}

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
public class WorkoutWeekResponse {
    private UUID weekId;
    private Integer weekNumber;
    private String notes;
    private List<WorkoutDayResponse> days;
    private Integer dayCount;
    private Boolean isCompleted; // All days completed
}

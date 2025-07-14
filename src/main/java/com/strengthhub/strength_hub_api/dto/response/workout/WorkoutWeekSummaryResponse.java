package com.strengthhub.strength_hub_api.dto.response.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutWeekSummaryResponse {
    private UUID weekId;
    private Integer weekNumber;
    private String notes;
    private Integer dayCount;
}

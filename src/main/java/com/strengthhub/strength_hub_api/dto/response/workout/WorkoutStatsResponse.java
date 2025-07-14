package com.strengthhub.strength_hub_api.dto.response.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutStatsResponse {
    private Integer totalWeeks;
    private Integer totalDays;
    private Integer totalExercises;
    private Integer totalSets;
    private Integer completedSets;
    private Double completionPercentage;
    private Integer currentWeek; // null if not started
    private Integer currentDay; // null if not started
}

package com.strengthhub.strength_hub_api.dto.response.workout;

import com.strengthhub.strength_hub_api.dto.response.lifter.LifterSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutProgressResponse {
    private UUID planId;
    private String planName;
    private LifterSummaryResponse lifter;
    private Integer totalWeeks;
    private Integer completedWeeks;
    private Integer currentWeek;
    private Integer currentDay;
    private Double overallProgress; // 0.0 to 100.0
    private LocalDateTime lastActivity;
    private Boolean isPlanCompleted;
}

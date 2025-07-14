package com.strengthhub.strength_hub_api.dto.response.workout;

import com.strengthhub.strength_hub_api.dto.response.coach.CoachSummaryResponse;
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
public class WorkoutPlanSummaryResponse {
    private UUID planId;
    private String name;
    private String description;
    private Integer totalWeeks;
    private Boolean isActive;
    private Boolean isTemplate;
    private LocalDateTime createdAt;
    private CoachSummaryResponse coach;
    private LifterSummaryResponse assignedLifter; // null if unassigned
}


package com.strengthhub.strength_hub_api.dto.response.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSetResponse {
    private UUID setId;
    private Integer setNumber;

    // Target values
    private Integer targetReps;
    private BigDecimal targetWeight;
    private BigDecimal targetRpe;

    // Actual values
    private Integer actualReps;
    private BigDecimal actualWeight;
    private BigDecimal actualRpe;

    private String lifterNotes;
    private Boolean isCompleted;
}
package com.strengthhub.strength_hub_api.dto.request.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetCompletionRequest {

    @NotNull(message = "Actual reps is required")
    @Min(value = 0, message = "Actual reps cannot be negative")
    private Integer actualReps;

    @NotNull(message = "Actual weight is required")
    @DecimalMin(value = "0.0", message = "Actual weight cannot be negative")
    private BigDecimal actualWeight;

    @NotNull(message = "Actual RPE is required")
    @DecimalMin(value = "6.0", message = "Actual RPE must be at least 6.0")
    @DecimalMax(value = "10.0", message = "Actual RPE cannot exceed 10.0")
    private BigDecimal actualRpe;

    @Size(max = 300, message = "Lifter notes cannot exceed 300 characters")
    private String lifterNotes;
}

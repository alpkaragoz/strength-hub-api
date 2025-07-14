package com.strengthhub.strength_hub_api.dto.request.coach;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachUpdateRequest {

    @Size(max = 5000, message = "Bio cannot exceed 5000 characters")
    private String bio;

    @Size(max = 2000, message = "Certifications cannot exceed 2000 characters")
    private String certifications;
}

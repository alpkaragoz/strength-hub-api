package com.strengthhub.strength_hub_api.dto.response.coach;

import com.strengthhub.strength_hub_api.dto.response.lifter.LifterSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachDetailResponse {
    private UUID coachId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private String bio;
    private String certifications;
    private List<LifterSummaryResponse> lifters;
}

package com.strengthhub.strength_hub_api.dto.response.coach;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachSummaryResponse {
    private UUID coachId;
    private String firstName;
    private String lastName;
    private String username;
}

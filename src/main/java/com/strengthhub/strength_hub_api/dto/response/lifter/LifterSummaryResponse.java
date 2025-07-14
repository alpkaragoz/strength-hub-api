package com.strengthhub.strength_hub_api.dto.response.lifter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifterSummaryResponse {
    private UUID lifterId;
    private String firstName;
    private String lastName;
    private String username;
}

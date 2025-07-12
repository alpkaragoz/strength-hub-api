package com.strengthhub.strength_hub_api.dto.response;

import com.strengthhub.strength_hub_api.enums.CoachCodeStatus;
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
public class CoachCodeResponse {
    private UUID codeId;
    private String code;
    private LocalDateTime expiresAt;
    private Boolean isUsed;
    private UUID usedBy;
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;
    private CoachCodeStatus status; // "ACTIVE", "EXPIRED", "USED"
}

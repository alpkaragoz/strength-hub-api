package com.strengthhub.strength_hub_api.dto.response.user;

import com.strengthhub.strength_hub_api.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isAdmin;
    private LocalDateTime createdAt;
    private Set<UserType> roles; // LIFTER, COACH based on profile existence
}

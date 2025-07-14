package com.strengthhub.strength_hub_api.dto.response.auth;

import com.strengthhub.strength_hub_api.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthenticationResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;

    // User information
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isAdmin;
    private Set<UserType> roles;
}

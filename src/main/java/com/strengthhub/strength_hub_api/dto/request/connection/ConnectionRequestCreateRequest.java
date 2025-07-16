package com.strengthhub.strength_hub_api.dto.request.connection;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionRequestCreateRequest {

    @NotNull(message = "Receiver ID is required")
    private UUID receiverId;

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;
}

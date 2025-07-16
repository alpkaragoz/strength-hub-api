package com.strengthhub.strength_hub_api.dto.request.connection;

import com.strengthhub.strength_hub_api.enums.ConnectionRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionRequestResponseRequest {

    @NotNull(message = "Status is required")
    private ConnectionRequestStatus status; // ACCEPTED or REJECTED

    @Size(max = 500, message = "Response message cannot exceed 500 characters")
    private String responseMessage;
}

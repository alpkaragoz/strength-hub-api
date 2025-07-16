package com.strengthhub.strength_hub_api.dto.response.connection;

import com.strengthhub.strength_hub_api.enums.ConnectionRequestStatus;
import com.strengthhub.strength_hub_api.enums.ConnectionRequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionRequestResponse {

    private UUID requestId;
    private UUID senderId;
    private String senderName;
    private String senderUsername;
    private UUID receiverId;
    private String receiverName;
    private String receiverUsername;
    private ConnectionRequestType type;
    private ConnectionRequestStatus status;
    private String message;
    private String responseMessage;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
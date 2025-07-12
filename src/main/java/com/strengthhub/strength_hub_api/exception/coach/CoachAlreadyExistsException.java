package com.strengthhub.strength_hub_api.exception.coach;

import java.util.UUID;

public class CoachAlreadyExistsException extends RuntimeException {
    public CoachAlreadyExistsException(String message) {
        super(message);
    }

    public CoachAlreadyExistsException(UUID userId) {
        super("Coach already exists for user id: " + userId);
    }
}
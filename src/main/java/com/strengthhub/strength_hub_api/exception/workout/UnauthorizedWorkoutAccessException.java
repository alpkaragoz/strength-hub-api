package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class UnauthorizedWorkoutAccessException extends RuntimeException {
    public UnauthorizedWorkoutAccessException(String message) {
        super(message);
    }

    public UnauthorizedWorkoutAccessException(UUID userId, UUID planId) {
        super("User " + userId + " is not authorized to access workout plan: " + planId);
    }

    public UnauthorizedWorkoutAccessException(String action, UUID userId) {
        super("User " + userId + " is not authorized to perform action: " + action);
    }
}

package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class InvalidWorkoutStructureException extends RuntimeException {
    public InvalidWorkoutStructureException(String message) {
        super(message);
    }

    public InvalidWorkoutStructureException(String operation, String reason) {
        super("Invalid workout structure for " + operation + ": " + reason);
    }

    public InvalidWorkoutStructureException(UUID planId, String reason) {
        super("Invalid structure for workout plan " + planId + ": " + reason);
    }
}
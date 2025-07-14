package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class WorkoutPlanInactiveException extends RuntimeException {
    public WorkoutPlanInactiveException(String message) {
        super(message);
    }

    public WorkoutPlanInactiveException(UUID planId) {
        super("Cannot modify inactive workout plan: " + planId);
    }
}
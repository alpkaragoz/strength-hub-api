package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class WorkoutPlanNotFoundException extends RuntimeException {
    public WorkoutPlanNotFoundException(String message) {
        super(message);
    }

    public WorkoutPlanNotFoundException(UUID planId) {
        super("Workout plan not found with id: " + planId);
    }
}

package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class WorkoutWeekNotFoundException extends RuntimeException {
    public WorkoutWeekNotFoundException(String message) {
        super(message);
    }

    public WorkoutWeekNotFoundException(UUID weekId) {
        super("Workout week not found with id: " + weekId);
    }

    public WorkoutWeekNotFoundException(UUID planId, Integer weekNumber) {
        super("Week " + weekNumber + " not found in workout plan: " + planId);
    }
}


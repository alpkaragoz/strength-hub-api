package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class ExerciseNotFoundException extends RuntimeException {
    public ExerciseNotFoundException(String message) {
        super(message);
    }

    public ExerciseNotFoundException(UUID exerciseId) {
        super("Exercise not found with id: " + exerciseId);
    }
}

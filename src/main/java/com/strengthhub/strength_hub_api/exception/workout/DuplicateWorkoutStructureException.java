package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class DuplicateWorkoutStructureException extends RuntimeException {
    public DuplicateWorkoutStructureException(String message) {
        super(message);
    }

    public DuplicateWorkoutStructureException(String type, Integer number, UUID parentId) {
        super(type + " " + number + " already exists in " + parentId);
    }
}


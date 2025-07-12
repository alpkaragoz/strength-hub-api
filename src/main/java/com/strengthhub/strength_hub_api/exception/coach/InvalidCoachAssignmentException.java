package com.strengthhub.strength_hub_api.exception.coach;

import java.util.UUID;

public class InvalidCoachAssignmentException extends RuntimeException {
    public InvalidCoachAssignmentException(String message) {
        super(message);
    }

    public InvalidCoachAssignmentException(UUID coachId) {
        super("Invalid coach assignment. Coach with id " + coachId + " does not exist");
    }
}

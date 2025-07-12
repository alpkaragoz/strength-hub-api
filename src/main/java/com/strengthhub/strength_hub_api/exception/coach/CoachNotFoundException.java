package com.strengthhub.strength_hub_api.exception.coach;

import java.util.UUID;

public class CoachNotFoundException extends RuntimeException {
    public CoachNotFoundException(String message) {
        super(message);
    }

    public CoachNotFoundException(UUID coachId) {
        super("Coach not found with id: " + coachId);
    }
}

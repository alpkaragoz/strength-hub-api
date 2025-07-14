package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class WorkoutPlanAlreadyAssignedException extends RuntimeException {
    public WorkoutPlanAlreadyAssignedException(String message) {
        super(message);
    }

    public WorkoutPlanAlreadyAssignedException(UUID planId, UUID lifterId) {
        super("Workout plan " + planId + " is already assigned to lifter: " + lifterId);
    }

    public WorkoutPlanAlreadyAssignedException(UUID lifterId) {
        super("Lifter " + lifterId + " already has an active workout plan assigned");
    }
}

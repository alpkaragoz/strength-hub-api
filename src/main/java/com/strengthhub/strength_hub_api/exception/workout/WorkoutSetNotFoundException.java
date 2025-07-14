package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class WorkoutSetNotFoundException extends RuntimeException {
  public WorkoutSetNotFoundException(String message) {
    super(message);
  }

  public WorkoutSetNotFoundException(UUID setId) {
    super("Workout set not found with id: " + setId);
  }

  public WorkoutSetNotFoundException(UUID exerciseId, Integer setNumber) {
    super("Set " + setNumber + " not found in exercise: " + exerciseId);
  }
}
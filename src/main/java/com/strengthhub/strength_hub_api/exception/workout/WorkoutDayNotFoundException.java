package com.strengthhub.strength_hub_api.exception.workout;

import java.util.UUID;

public class WorkoutDayNotFoundException extends RuntimeException {
  public WorkoutDayNotFoundException(String message) {
    super(message);
  }

  public WorkoutDayNotFoundException(UUID dayId) {
    super("Workout day not found with id: " + dayId);
  }

  public WorkoutDayNotFoundException(UUID weekId, Integer dayNumber) {
    super("Day " + dayNumber + " not found in workout week: " + weekId);
  }
}

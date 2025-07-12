package com.strengthhub.strength_hub_api.exception.lifter;

import java.util.UUID;

public class LifterNotFoundException extends RuntimeException {
  public LifterNotFoundException(String message) {
    super(message);
  }

  public LifterNotFoundException(UUID lifterId) {
    super("Lifter not found with id: " + lifterId);
  }
}
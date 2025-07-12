package com.strengthhub.strength_hub_api.exception.user;

import com.strengthhub.strength_hub_api.enums.UserType;

public class InvalidUserTypeException extends RuntimeException {
  public InvalidUserTypeException(String message) {
    super(message);
  }

  public InvalidUserTypeException(Integer userId, UserType requiredType) {
    super("User with id " + userId + " must have user type " + requiredType);
  }
}

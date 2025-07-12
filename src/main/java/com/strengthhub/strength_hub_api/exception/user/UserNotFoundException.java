package com.strengthhub.strength_hub_api.exception.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Integer userId) {
        super("User not found with id: " + userId);
    }
}

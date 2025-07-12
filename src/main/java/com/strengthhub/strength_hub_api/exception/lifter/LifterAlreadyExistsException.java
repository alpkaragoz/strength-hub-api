package com.strengthhub.strength_hub_api.exception.lifter;

import java.util.UUID;

public class LifterAlreadyExistsException extends RuntimeException {
    public LifterAlreadyExistsException(String message) {
        super(message);
    }

    public LifterAlreadyExistsException(UUID userId) {
        super("Lifter already exists for user id: " + userId);
    }
}
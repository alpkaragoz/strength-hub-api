package com.strengthhub.strength_hub_api.exception.common;

/**
 * Exception thrown when access is forbidden (403 Forbidden scenarios).
 * Used when user is authenticated but lacks necessary permissions.
 */
public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException(String message) {
        super(message);
    }
}

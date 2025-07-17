package com.strengthhub.strength_hub_api.exception.auth;

/**
 * Exception thrown when authentication fails (401 Unauthorized scenarios).
 * Used for missing, invalid, or expired credentials.
 */
public class AuthenticationFailedException extends RuntimeException {

  public AuthenticationFailedException(String message) {
    super(message);
  }

  public AuthenticationFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  // Factory methods for common authentication failures
  public static AuthenticationFailedException missingCredentials() {
    return new AuthenticationFailedException("Authentication credentials are required");
  }

  public static AuthenticationFailedException invalidCredentials() {
    return new AuthenticationFailedException("Invalid authentication credentials");
  }

  public static AuthenticationFailedException expiredToken() {
    return new AuthenticationFailedException("Authentication token has expired");
  }

  public static AuthenticationFailedException invalidToken() {
    return new AuthenticationFailedException("Invalid authentication token");
  }

  public static AuthenticationFailedException malformedToken() {
    return new AuthenticationFailedException("Malformed authentication token");
  }
}
package com.strengthhub.strength_hub_api.exception.connection;

import java.util.UUID;

public class ConnectionRequestNotFoundException extends RuntimeException {
  public ConnectionRequestNotFoundException(String message) {
    super(message);
  }

  public ConnectionRequestNotFoundException(UUID requestId) {
    super("Connection request not found with id: " + requestId);
  }
}

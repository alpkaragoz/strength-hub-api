package com.strengthhub.strength_hub_api.exception.connection;

import java.util.UUID;

public class DuplicateConnectionRequestException extends RuntimeException {
  public DuplicateConnectionRequestException(String message) {
    super(message);
  }

  public DuplicateConnectionRequestException(UUID senderId, UUID receiverId) {
    super("Connection request already exists between users: " + senderId + " and " + receiverId);
  }
}

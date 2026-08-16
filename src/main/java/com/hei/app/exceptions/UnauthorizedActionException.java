package com.hei.app.exceptions;

public class UnauthorizedActionException extends RuntimeException {
  public UnauthorizedActionException(String message) {
    super(message);
  }
}

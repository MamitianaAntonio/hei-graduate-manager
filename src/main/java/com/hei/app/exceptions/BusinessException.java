package com.hei.app.exceptions;

public class BusinessException extends RuntimeException {
  public BusinessException(String message) {
    super(message);
  }
}

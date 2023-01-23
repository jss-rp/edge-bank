package com.jss.bank.edge.security.exception;

public class UserNotAllowedException extends RuntimeException {

  public UserNotAllowedException(String message) {
    super(message);
  }
}

package com.jss.bank.edge.security.exception;

public class InvalidBearerTokenException extends RuntimeException {

  public InvalidBearerTokenException(Throwable cause) {
    super(cause);
  }
}

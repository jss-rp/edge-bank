package com.jss.bank.edge.util;

public enum EventBusFailureCode {

  UNKOWN_ERROR(0),
  MESSAGE_BODY_CLASS_NOT_EXPECTED(1),
  MESSAGE_WITHOUT_BODY(2);

  private final int code;

  EventBusFailureCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }
}

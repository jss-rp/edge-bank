package com.jss.bank.edge.util;

public enum EventBusFailureCode {

  MESSAGE_BODY_CLASS_NOT_EXPECTED(1),
  MESSAGE_WITHOUT_BODY(2);

  EventBusFailureCode(int i) {}
}

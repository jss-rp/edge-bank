package com.jss.bank.edge.util;

public enum IntegratedService {
  DATABASE("mysql");

  private final String serviceName;

  IntegratedService(final String serviceName) {
    this.serviceName = serviceName;
  }

  public String getName() {
    return serviceName;
  }
}

package com.jss.bank.edge.util;

public enum Environment {
  DEV, STG, HOM, PROD;

  public static Environment fromString(final String raw) {
    var lowerCaseRaw = raw.toLowerCase();

    return switch (lowerCaseRaw) {
      case "dev", "development" -> DEV;
      case "stg", "staging" -> STG;
      case "hom", "homologation" -> HOM;
      case "prod", "production" -> PROD;
      default -> throw new IllegalStateException("Unexpected value: " + lowerCaseRaw);
    };
  }
}

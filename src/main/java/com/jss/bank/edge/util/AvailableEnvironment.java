package com.jss.bank.edge.util;

import lombok.val;

public enum AvailableEnvironment {
  DEV, STG, HOM, PROD;

  public static AvailableEnvironment fromString(final String raw) {
    val lowerCaseRaw = raw.toLowerCase();
    return switch (lowerCaseRaw) {
      case "dev", "development" -> DEV;
      case "stg", "staging" -> STG;
      case "hom", "homologation" -> HOM;
      case "prod", "production" -> PROD;
      default -> throw new IllegalStateException("Unexpected value: " + lowerCaseRaw);
    };
  }
}

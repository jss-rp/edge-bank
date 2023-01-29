package com.jss.bank.edge.domain.dto;

import java.math.BigDecimal;

public record AccountDTO (
    String agency,
    String code,
    String dtVerifier,
    BigDecimal balance,
    String username
) { }

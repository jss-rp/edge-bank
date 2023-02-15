package com.jss.bank.edge.domain.dto;

import java.time.LocalDate;

public record PersonDTO(
        String firstName,
        String surname,
        LocalDate birthDate,
        String document
) { }

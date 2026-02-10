package com.sassveterinaria.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PetResponse(
    UUID id,
    UUID branchId,
    UUID clientId,
    String internalCode,
    String name,
    String species,
    String breed,
    String sex,
    LocalDate birthDate,
    BigDecimal weightKg,
    Boolean neutered,
    String alerts,
    String history,
    OffsetDateTime createdAt
) {
}

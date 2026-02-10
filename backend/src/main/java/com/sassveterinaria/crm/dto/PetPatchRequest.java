package com.sassveterinaria.crm.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PetPatchRequest(
    @Size(max = 30) String internalCode,
    @Size(max = 120) String name,
    @Size(max = 80) String species,
    @Size(max = 120) String breed,
    @Size(max = 20) String sex,
    LocalDate birthDate,
    @DecimalMin(value = "0.01", inclusive = true) @DecimalMax(value = "9999.99", inclusive = true) BigDecimal weightKg,
    Boolean neutered,
    String alerts,
    String history
) {
}

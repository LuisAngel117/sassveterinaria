package com.sassveterinaria.crm.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PetCreateRequest(
    @NotBlank @Size(max = 30) String internalCode,
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 80) String species,
    @Size(max = 120) String breed,
    @Size(max = 20) String sex,
    LocalDate birthDate,
    @DecimalMin(value = "0.01", inclusive = true) @DecimalMax(value = "9999.99", inclusive = true) BigDecimal weightKg,
    Boolean neutered,
    String alerts,
    String history
) {
}

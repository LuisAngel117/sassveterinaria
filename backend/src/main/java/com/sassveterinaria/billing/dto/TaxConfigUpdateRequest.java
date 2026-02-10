package com.sassveterinaria.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TaxConfigUpdateRequest(
    @NotNull BigDecimal taxRate,
    @NotBlank String reason
) {
}

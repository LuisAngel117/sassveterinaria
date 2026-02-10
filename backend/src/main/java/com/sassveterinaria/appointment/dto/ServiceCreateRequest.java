package com.sassveterinaria.appointment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ServiceCreateRequest(
    @NotBlank @Size(min = 3, max = 120) String name,
    @NotNull @Min(5) @Max(480) Integer durationMinutes,
    @NotNull @DecimalMin(value = "0.00", inclusive = true) @DecimalMax(value = "99999.99", inclusive = true) BigDecimal priceBase
) {
}

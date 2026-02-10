package com.sassveterinaria.appointment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ServicePatchRequest(
    @Size(min = 3, max = 120) String name,
    @Min(5) @Max(480) Integer durationMinutes,
    @DecimalMin(value = "0.00", inclusive = true) @DecimalMax(value = "99999.99", inclusive = true) BigDecimal priceBase,
    Boolean isActive,
    String reason
) {
}

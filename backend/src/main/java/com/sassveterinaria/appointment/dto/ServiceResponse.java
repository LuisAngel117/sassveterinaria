package com.sassveterinaria.appointment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ServiceResponse(
    UUID id,
    UUID branchId,
    String name,
    int durationMinutes,
    BigDecimal priceBase,
    boolean isActive,
    OffsetDateTime createdAt
) {
}

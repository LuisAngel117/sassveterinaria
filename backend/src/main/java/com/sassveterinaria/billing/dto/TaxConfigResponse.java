package com.sassveterinaria.billing.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaxConfigResponse(
    UUID id,
    UUID branchId,
    BigDecimal taxRate,
    UUID updatedBy,
    OffsetDateTime updatedAt
) {
}

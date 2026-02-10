package com.sassveterinaria.inventory.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StockMovementResponse(
    UUID id,
    UUID branchId,
    UUID productId,
    String type,
    BigDecimal qty,
    BigDecimal unitCost,
    BigDecimal totalCost,
    String reason,
    UUID visitId,
    UUID createdBy,
    OffsetDateTime createdAt,
    BigDecimal onHandAfter,
    BigDecimal avgUnitCostAfter
) {
}

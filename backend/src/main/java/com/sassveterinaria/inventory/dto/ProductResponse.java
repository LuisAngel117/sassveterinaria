package com.sassveterinaria.inventory.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    UUID branchId,
    String sku,
    String name,
    UUID unitId,
    String unitCode,
    String unitName,
    BigDecimal minQty,
    boolean isActive,
    BigDecimal onHandQty,
    BigDecimal avgUnitCost,
    boolean lowStock,
    OffsetDateTime createdAt,
    OffsetDateTime stockUpdatedAt
) {
}

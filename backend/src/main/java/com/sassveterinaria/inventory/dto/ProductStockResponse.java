package com.sassveterinaria.inventory.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductStockResponse(
    UUID productId,
    UUID branchId,
    BigDecimal onHandQty,
    BigDecimal avgUnitCost,
    BigDecimal minQty,
    boolean lowStock,
    OffsetDateTime updatedAt
) {
}

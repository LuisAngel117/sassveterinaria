package com.sassveterinaria.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockMovementCreateRequest(
    UUID productId,
    String type,
    BigDecimal qty,
    BigDecimal qtyDelta,
    BigDecimal unitCost,
    Boolean override,
    String reason
) {
}

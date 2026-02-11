package com.sassveterinaria.reports.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryConsumptionItem(
    String groupBy,
    UUID entityId,
    String label,
    BigDecimal qty,
    BigDecimal totalCost
) {
}

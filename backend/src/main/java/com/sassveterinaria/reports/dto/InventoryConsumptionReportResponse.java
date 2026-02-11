package com.sassveterinaria.reports.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record InventoryConsumptionReportResponse(
    OffsetDateTime from,
    OffsetDateTime to,
    String groupBy,
    BigDecimal totalQty,
    BigDecimal totalCost,
    List<InventoryConsumptionItem> items
) {
}

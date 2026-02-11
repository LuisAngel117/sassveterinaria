package com.sassveterinaria.reports.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record TopServicesReportResponse(
    OffsetDateTime from,
    OffsetDateTime to,
    String metric,
    int limit,
    BigDecimal totalValue,
    List<TopServiceItem> items
) {
}

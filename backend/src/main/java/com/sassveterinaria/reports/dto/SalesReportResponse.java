package com.sassveterinaria.reports.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record SalesReportResponse(
    OffsetDateTime from,
    OffsetDateTime to,
    String groupBy,
    BigDecimal totalFacturado,
    BigDecimal totalImpuesto,
    BigDecimal totalCobrado,
    List<ReportSeriesPoint> series,
    List<ReportBreakdownItem> breakdown
) {
}

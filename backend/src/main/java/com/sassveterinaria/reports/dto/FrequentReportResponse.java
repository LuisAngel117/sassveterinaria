package com.sassveterinaria.reports.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record FrequentReportResponse(
    OffsetDateTime from,
    OffsetDateTime to,
    String dimension,
    int limit,
    long totalVisits,
    List<FrequentItem> items
) {
}

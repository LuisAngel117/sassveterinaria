package com.sassveterinaria.reports.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AppointmentsReportResponse(
    OffsetDateTime from,
    OffsetDateTime to,
    String groupBy,
    long totalAppointments,
    List<ReportSeriesPoint> series,
    List<AppointmentsReportItem> items,
    int page,
    int size,
    long total
) {
}

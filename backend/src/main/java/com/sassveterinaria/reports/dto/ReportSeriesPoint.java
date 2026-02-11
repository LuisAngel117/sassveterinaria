package com.sassveterinaria.reports.dto;

import java.math.BigDecimal;

public record ReportSeriesPoint(
    String key,
    BigDecimal value
) {
}

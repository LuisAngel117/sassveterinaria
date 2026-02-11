package com.sassveterinaria.reports.dto;

import java.math.BigDecimal;

public record ReportBreakdownItem(
    String key,
    BigDecimal value
) {
}

package com.sassveterinaria.reports.dto;

import java.util.Arrays;

public enum ReportGroupBy {
    DAY,
    WEEK;

    public static ReportGroupBy fromValue(String value) {
        if (value == null || value.isBlank()) {
            return DAY;
        }
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

package com.sassveterinaria.reports.dto;

import java.util.Arrays;

public enum TopServiceMetric {
    COUNT,
    REVENUE;

    public static TopServiceMetric fromValue(String value) {
        if (value == null || value.isBlank()) {
            return COUNT;
        }
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

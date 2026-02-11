package com.sassveterinaria.reports.dto;

import java.util.Arrays;

public enum FrequentDimension {
    CLIENT,
    PET;

    public static FrequentDimension fromValue(String value) {
        if (value == null || value.isBlank()) {
            return CLIENT;
        }
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

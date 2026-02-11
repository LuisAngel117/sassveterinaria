package com.sassveterinaria.reports.dto;

import java.util.Arrays;

public enum InventoryGroupBy {
    PRODUCT,
    SERVICE;

    public static InventoryGroupBy fromValue(String value) {
        if (value == null || value.isBlank()) {
            return PRODUCT;
        }
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

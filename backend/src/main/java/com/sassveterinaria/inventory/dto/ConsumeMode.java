package com.sassveterinaria.inventory.dto;

import java.util.Arrays;

public enum ConsumeMode {
    BOM_ONLY,
    EXPLICIT_ONLY,
    BOM_PLUS_EXPLICIT;

    public static ConsumeMode fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
            .filter(mode -> mode.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

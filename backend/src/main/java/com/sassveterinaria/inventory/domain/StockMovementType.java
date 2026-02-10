package com.sassveterinaria.inventory.domain;

import java.util.Arrays;

public enum StockMovementType {
    IN,
    OUT,
    ADJUST,
    CONSUME;

    public static StockMovementType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

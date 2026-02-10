package com.sassveterinaria.clinical.domain;

import java.util.Arrays;

public enum VisitStatus {
    OPEN,
    CLOSED;

    public static VisitStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

package com.sassveterinaria.billing.domain;

import java.util.Arrays;

public enum InvoiceStatus {
    PENDING,
    PAID,
    VOID;

    public static InvoiceStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

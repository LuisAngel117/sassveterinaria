package com.sassveterinaria.billing.domain;

import java.util.Arrays;

public enum PaymentMethod {
    CASH,
    CARD,
    TRANSFER;

    public static PaymentMethod fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

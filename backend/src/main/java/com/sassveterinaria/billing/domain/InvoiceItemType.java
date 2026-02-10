package com.sassveterinaria.billing.domain;

import java.util.Arrays;

public enum InvoiceItemType {
    SERVICE,
    PRODUCT;

    public static InvoiceItemType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

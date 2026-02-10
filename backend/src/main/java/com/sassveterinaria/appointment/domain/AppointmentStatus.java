package com.sassveterinaria.appointment.domain;

import java.util.Arrays;

public enum AppointmentStatus {
    RESERVED,
    CONFIRMED,
    IN_ATTENTION,
    CLOSED,
    CANCELLED;

    public static AppointmentStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}

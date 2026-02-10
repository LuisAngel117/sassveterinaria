package com.sassveterinaria.appointment.dto;

import java.util.UUID;

public record ServiceResponse(
    UUID id,
    UUID branchId,
    String name,
    int durationMinutes
) {
}

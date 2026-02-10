package com.sassveterinaria.appointment.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentUpdateRequest(
    @NotNull UUID roomId,
    @NotNull OffsetDateTime startsAt,
    String overbookReason
) {
}

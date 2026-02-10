package com.sassveterinaria.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentCreateRequest(
    @NotNull OffsetDateTime startsAt,
    @NotNull OffsetDateTime endsAt,
    @NotBlank String status,
    String reason,
    String notes,
    UUID roomId,
    UUID clientId,
    UUID petId,
    UUID veterinarianId
) {
}

package com.sassveterinaria.appointment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
    UUID id,
    UUID branchId,
    OffsetDateTime startsAt,
    OffsetDateTime endsAt,
    String status,
    String reason,
    String notes,
    UUID roomId,
    UUID clientId,
    UUID petId,
    UUID veterinarianId,
    OffsetDateTime createdAt
) {
}

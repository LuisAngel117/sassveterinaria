package com.sassveterinaria.appointment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RoomBlockResponse(
    UUID id,
    UUID branchId,
    UUID roomId,
    OffsetDateTime startsAt,
    OffsetDateTime endsAt,
    String reason,
    UUID createdBy,
    OffsetDateTime createdAt
) {
}

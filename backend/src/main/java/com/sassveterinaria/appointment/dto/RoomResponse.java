package com.sassveterinaria.appointment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RoomResponse(
    UUID id,
    UUID branchId,
    String name,
    boolean isActive,
    OffsetDateTime createdAt
) {
}

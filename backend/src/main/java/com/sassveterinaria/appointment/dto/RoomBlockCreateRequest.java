package com.sassveterinaria.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RoomBlockCreateRequest(
    @NotNull UUID roomId,
    @NotNull OffsetDateTime startsAt,
    @NotNull OffsetDateTime endsAt,
    @NotBlank String reason
) {
}

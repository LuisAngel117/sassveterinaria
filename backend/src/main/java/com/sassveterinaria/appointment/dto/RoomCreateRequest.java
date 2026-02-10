package com.sassveterinaria.appointment.dto;

import jakarta.validation.constraints.NotBlank;

public record RoomCreateRequest(
    @NotBlank String name
) {
}

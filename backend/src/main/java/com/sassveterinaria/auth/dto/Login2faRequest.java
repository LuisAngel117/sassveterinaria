package com.sassveterinaria.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record Login2faRequest(
    @NotBlank String challengeToken,
    @NotBlank String code
) {
}

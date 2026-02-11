package com.sassveterinaria.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorEnableRequest(
    @NotBlank String code
) {
}

package com.sassveterinaria.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PrescriptionCreateRequest(
    @NotBlank @Size(max = 160) String medication,
    @NotBlank @Size(max = 60) String dose,
    @NotBlank @Size(max = 30) String unit,
    @NotBlank @Size(max = 60) String frequency,
    @NotBlank @Size(max = 60) String duration,
    @NotBlank @Size(max = 60) String route,
    String notes
) {
}

package com.sassveterinaria.clinical.dto;

import jakarta.validation.constraints.Size;

public record PrescriptionPatchRequest(
    @Size(max = 160) String medication,
    @Size(max = 60) String dose,
    @Size(max = 30) String unit,
    @Size(max = 60) String frequency,
    @Size(max = 60) String duration,
    @Size(max = 60) String route,
    String notes
) {
}

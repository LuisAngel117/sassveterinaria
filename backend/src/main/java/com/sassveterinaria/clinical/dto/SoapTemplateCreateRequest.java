package com.sassveterinaria.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SoapTemplateCreateRequest(
    @NotNull UUID serviceId,
    @NotBlank @Size(min = 3, max = 120) String name,
    String sReason,
    String sAnamnesis,
    String oFindings,
    String aDiagnosis,
    String pTreatment,
    String pInstructions
) {
}

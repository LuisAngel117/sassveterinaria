package com.sassveterinaria.clinical.dto;

import jakarta.validation.constraints.Size;

public record SoapTemplatePatchRequest(
    @Size(min = 3, max = 120) String name,
    String sReason,
    String sAnamnesis,
    String oFindings,
    String aDiagnosis,
    String pTreatment,
    String pInstructions,
    Boolean isActive
) {
}

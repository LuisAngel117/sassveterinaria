package com.sassveterinaria.clinical.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SoapTemplateResponse(
    UUID id,
    UUID branchId,
    UUID serviceId,
    String name,
    String sReason,
    String sAnamnesis,
    String oFindings,
    String aDiagnosis,
    String pTreatment,
    String pInstructions,
    boolean isActive,
    OffsetDateTime createdAt
) {
}

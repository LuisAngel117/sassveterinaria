package com.sassveterinaria.clinical.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record VisitResponse(
    UUID id,
    UUID branchId,
    UUID petId,
    UUID serviceId,
    UUID appointmentId,
    String status,
    String sReason,
    String sAnamnesis,
    BigDecimal oWeightKg,
    BigDecimal oTemperatureC,
    String oFindings,
    String aDiagnosis,
    String aSeverity,
    String pTreatment,
    String pInstructions,
    LocalDate pFollowupAt,
    UUID createdBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}

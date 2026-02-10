package com.sassveterinaria.clinical.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VisitCreateRequest(
    @NotNull UUID petId,
    @NotNull UUID serviceId,
    UUID appointmentId,
    UUID templateId,
    String sReason,
    String sAnamnesis,
    BigDecimal oWeightKg,
    BigDecimal oTemperatureC,
    String oFindings,
    String aDiagnosis,
    String aSeverity,
    String pTreatment,
    String pInstructions,
    LocalDate pFollowupAt
) {
}

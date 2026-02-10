package com.sassveterinaria.clinical.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VisitPatchRequest(
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

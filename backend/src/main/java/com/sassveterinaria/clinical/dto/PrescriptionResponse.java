package com.sassveterinaria.clinical.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PrescriptionResponse(
    UUID id,
    UUID branchId,
    UUID visitId,
    String medication,
    String dose,
    String unit,
    String frequency,
    String duration,
    String route,
    String notes,
    OffsetDateTime createdAt
) {
}

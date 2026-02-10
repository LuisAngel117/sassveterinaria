package com.sassveterinaria.clinical.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VisitAttachmentResponse(
    UUID id,
    UUID branchId,
    UUID visitId,
    String originalFilename,
    String contentType,
    long sizeBytes,
    UUID createdBy,
    OffsetDateTime createdAt
) {
}

package com.sassveterinaria.audit.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditEventResponse(
    UUID id,
    OffsetDateTime createdAt,
    UUID actorUserId,
    String actorUsername,
    UUID branchId,
    String action,
    String entityType,
    UUID entityId,
    boolean isSensitive,
    String reason,
    String beforeJson,
    String afterJson,
    String ip,
    String userAgent
) {
}

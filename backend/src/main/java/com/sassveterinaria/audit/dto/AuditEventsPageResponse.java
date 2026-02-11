package com.sassveterinaria.audit.dto;

import java.util.List;

public record AuditEventsPageResponse(
    List<AuditEventResponse> items,
    int page,
    int size,
    long total
) {
}

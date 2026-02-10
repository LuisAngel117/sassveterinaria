package com.sassveterinaria.crm.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientResponse(
    UUID id,
    UUID branchId,
    String fullName,
    String identification,
    String phone,
    String email,
    String address,
    String notes,
    OffsetDateTime createdAt
) {
}

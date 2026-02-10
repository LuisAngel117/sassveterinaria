package com.sassveterinaria.auth.dto;

import java.util.List;
import java.util.UUID;

public record MeResponse(
    UUID id,
    String username,
    String fullName,
    String roleCode,
    UUID branchId,
    List<String> permissions
) {
}

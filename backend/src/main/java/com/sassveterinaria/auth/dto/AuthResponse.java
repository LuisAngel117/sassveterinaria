package com.sassveterinaria.auth.dto;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresInSeconds,
    UserPayload user,
    BranchPayload branch
) {
    public record UserPayload(UUID id, String username, String fullName, String roleCode) {
    }

    public record BranchPayload(UUID id, String code, String name) {
    }
}

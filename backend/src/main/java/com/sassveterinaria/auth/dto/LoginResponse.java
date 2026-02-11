package com.sassveterinaria.auth.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    Long expiresInSeconds,
    AuthResponse.UserPayload user,
    AuthResponse.BranchPayload branch,
    Boolean challengeRequired,
    String challengeToken,
    Long challengeExpiresInSeconds,
    Boolean requiresTotpSetup,
    String message
) {
}

package com.sassveterinaria.auth.dto;

public record TwoFactorSetupResponse(
    String secret,
    String otpauthUri,
    String issuer,
    String accountName
) {
}

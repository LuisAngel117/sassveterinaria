package com.sassveterinaria.inventory.dto;

import java.util.UUID;

public record UnitResponse(
    UUID id,
    String code,
    String name,
    boolean isActive
) {
}

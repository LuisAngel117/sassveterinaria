package com.sassveterinaria.inventory.dto;

import java.util.List;
import java.util.UUID;

public record InventoryConsumeResponse(
    UUID visitId,
    String mode,
    List<StockMovementResponse> movements
) {
}

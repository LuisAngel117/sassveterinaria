package com.sassveterinaria.inventory.dto;

import java.util.List;

public record InventoryConsumeRequest(
    String mode,
    List<InventoryConsumeItemRequest> items,
    Boolean override,
    String reason
) {
}

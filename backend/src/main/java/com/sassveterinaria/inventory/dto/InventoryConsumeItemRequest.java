package com.sassveterinaria.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryConsumeItemRequest(
    UUID productId,
    BigDecimal qty
) {
}

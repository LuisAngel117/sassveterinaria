package com.sassveterinaria.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceBomItemResponse(
    UUID id,
    UUID branchId,
    UUID serviceId,
    UUID productId,
    String productName,
    String productSku,
    BigDecimal qty
) {
}

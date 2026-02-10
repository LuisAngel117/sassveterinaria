package com.sassveterinaria.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LowStockResponse(
    UUID productId,
    String productName,
    String sku,
    String unitCode,
    BigDecimal onHandQty,
    BigDecimal minQty
) {
}

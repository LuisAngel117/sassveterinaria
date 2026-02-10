package com.sassveterinaria.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductPatchRequest(
    String sku,
    String name,
    UUID unitId,
    BigDecimal minQty,
    Boolean isActive
) {
}

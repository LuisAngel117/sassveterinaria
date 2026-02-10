package com.sassveterinaria.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceCreateItemRequest(
    @NotBlank String itemType,
    @NotNull UUID itemId,
    String description,
    @NotNull BigDecimal qty,
    BigDecimal unitPrice,
    BigDecimal discountAmount
) {
}

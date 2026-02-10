package com.sassveterinaria.billing.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InvoiceItemResponse(
    UUID id,
    UUID branchId,
    UUID invoiceId,
    String itemType,
    UUID itemId,
    String description,
    BigDecimal qty,
    BigDecimal unitPrice,
    BigDecimal discountAmount,
    BigDecimal lineTotal,
    OffsetDateTime createdAt
) {
}

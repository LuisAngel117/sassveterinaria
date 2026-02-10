package com.sassveterinaria.billing.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InvoiceResponse(
    UUID id,
    UUID branchId,
    UUID visitId,
    String invoiceNumber,
    String status,
    BigDecimal itemsSubtotal,
    BigDecimal discountAmount,
    BigDecimal taxRate,
    BigDecimal taxAmount,
    BigDecimal total,
    BigDecimal paidTotal,
    String voidReason,
    OffsetDateTime voidedAt,
    UUID createdBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}

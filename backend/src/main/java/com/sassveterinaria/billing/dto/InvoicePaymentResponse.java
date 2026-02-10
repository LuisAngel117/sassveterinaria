package com.sassveterinaria.billing.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InvoicePaymentResponse(
    UUID id,
    UUID branchId,
    UUID invoiceId,
    String method,
    BigDecimal amount,
    String reference,
    UUID createdBy,
    OffsetDateTime createdAt
) {
}

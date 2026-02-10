package com.sassveterinaria.billing.dto;

import java.math.BigDecimal;

public record InvoiceItemPatchRequest(
    String description,
    BigDecimal qty,
    BigDecimal unitPrice,
    BigDecimal discountAmount,
    String reason
) {
}

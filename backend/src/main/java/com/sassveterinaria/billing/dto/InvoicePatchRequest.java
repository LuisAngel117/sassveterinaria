package com.sassveterinaria.billing.dto;

import java.math.BigDecimal;

public record InvoicePatchRequest(
    BigDecimal discountAmount,
    String reason
) {
}

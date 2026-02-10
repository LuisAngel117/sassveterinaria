package com.sassveterinaria.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InvoicePaymentCreateRequest(
    @NotBlank String method,
    @NotNull BigDecimal amount,
    String reference
) {
}

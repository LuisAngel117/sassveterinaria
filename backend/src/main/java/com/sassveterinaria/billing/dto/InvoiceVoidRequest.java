package com.sassveterinaria.billing.dto;

import jakarta.validation.constraints.NotBlank;

public record InvoiceVoidRequest(
    @NotBlank String reason
) {
}

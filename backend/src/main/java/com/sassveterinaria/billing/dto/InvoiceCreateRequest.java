package com.sassveterinaria.billing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public record InvoiceCreateRequest(
    @NotEmpty List<@Valid InvoiceCreateItemRequest> items,
    BigDecimal discountAmount
) {
}

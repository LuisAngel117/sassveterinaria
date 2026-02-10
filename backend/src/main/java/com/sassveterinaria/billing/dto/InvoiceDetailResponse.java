package com.sassveterinaria.billing.dto;

import java.util.List;

public record InvoiceDetailResponse(
    InvoiceResponse invoice,
    List<InvoiceItemResponse> items,
    List<InvoicePaymentResponse> payments
) {
}

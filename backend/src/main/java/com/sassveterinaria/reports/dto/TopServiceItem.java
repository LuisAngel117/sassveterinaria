package com.sassveterinaria.reports.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopServiceItem(
    UUID serviceId,
    String serviceName,
    BigDecimal count,
    BigDecimal revenue
) {
}

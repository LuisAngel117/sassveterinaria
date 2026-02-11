package com.sassveterinaria.reports.dto;

import java.util.UUID;

public record FrequentItem(
    UUID entityId,
    String displayName,
    long count
) {
}

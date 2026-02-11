package com.sassveterinaria.reports.dto;

public record ReportExportPayload(
    String filename,
    byte[] content
) {
}

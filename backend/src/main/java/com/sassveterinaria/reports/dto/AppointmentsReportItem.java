package com.sassveterinaria.reports.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentsReportItem(
    UUID appointmentId,
    OffsetDateTime start,
    OffsetDateTime end,
    String status,
    UUID roomId,
    String roomName,
    UUID clientId,
    String clientName,
    UUID petId,
    String petName
) {
}

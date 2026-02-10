package com.sassveterinaria.appointment.api;

import com.sassveterinaria.appointment.dto.AppointmentCancelRequest;
import com.sassveterinaria.appointment.dto.AppointmentCreateRequest;
import com.sassveterinaria.appointment.dto.AppointmentResponse;
import com.sassveterinaria.appointment.dto.AppointmentUpdateRequest;
import com.sassveterinaria.appointment.service.AppointmentService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('APPT_READ')")
    public ResponseEntity<List<AppointmentResponse>> list(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
        @RequestParam(value = "roomId", required = false) UUID roomId,
        @RequestParam(value = "status", required = false) String status
    ) {
        return ResponseEntity.ok(appointmentService.list(principal, from, to, roomId, status));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('APPT_CREATE')")
    public ResponseEntity<AppointmentResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody AppointmentCreateRequest request
    ) {
        return ResponseEntity.ok(appointmentService.create(principal, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('APPT_UPDATE')")
    public ResponseEntity<AppointmentResponse> reschedule(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID appointmentId,
        @Valid @RequestBody AppointmentUpdateRequest request
    ) {
        return ResponseEntity.ok(appointmentService.reschedule(principal, appointmentId, request));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('APPT_START_VISIT')")
    public ResponseEntity<AppointmentResponse> confirm(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID appointmentId
    ) {
        return ResponseEntity.ok(appointmentService.confirm(principal, appointmentId));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('APPT_START_VISIT')")
    public ResponseEntity<AppointmentResponse> start(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID appointmentId
    ) {
        return ResponseEntity.ok(appointmentService.start(principal, appointmentId));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('APPT_CLOSE')")
    public ResponseEntity<AppointmentResponse> close(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID appointmentId
    ) {
        return ResponseEntity.ok(appointmentService.close(principal, appointmentId));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('APPT_CANCEL')")
    public ResponseEntity<AppointmentResponse> cancel(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID appointmentId,
        @RequestBody(required = false) AppointmentCancelRequest request
    ) {
        String reason = request == null ? null : request.reason();
        return ResponseEntity.ok(appointmentService.cancel(principal, appointmentId, reason));
    }

    @PostMapping("/{id}/checkin")
    @PreAuthorize("hasAuthority('APPT_CHECKIN')")
    public ResponseEntity<AppointmentResponse> checkin(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID appointmentId
    ) {
        return ResponseEntity.ok(appointmentService.checkin(principal, appointmentId));
    }
}

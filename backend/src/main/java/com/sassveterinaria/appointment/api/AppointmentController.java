package com.sassveterinaria.appointment.api;

import com.sassveterinaria.appointment.dto.AppointmentCreateRequest;
import com.sassveterinaria.appointment.dto.AppointmentResponse;
import com.sassveterinaria.appointment.service.AppointmentService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return ResponseEntity.ok(appointmentService.list(principal, from, to));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('APPT_CREATE')")
    public ResponseEntity<AppointmentResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody AppointmentCreateRequest request
    ) {
        return ResponseEntity.ok(appointmentService.create(principal, request));
    }
}

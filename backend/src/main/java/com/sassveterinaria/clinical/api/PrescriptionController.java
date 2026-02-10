package com.sassveterinaria.clinical.api;

import com.sassveterinaria.clinical.dto.PrescriptionCreateRequest;
import com.sassveterinaria.clinical.dto.PrescriptionPatchRequest;
import com.sassveterinaria.clinical.dto.PrescriptionResponse;
import com.sassveterinaria.clinical.service.PrescriptionService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/visits/{id}/prescriptions")
    @PreAuthorize("hasAuthority('VISIT_UPDATE')")
    public ResponseEntity<PrescriptionResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID visitId,
        @Valid @RequestBody PrescriptionCreateRequest request
    ) {
        return ResponseEntity.ok(prescriptionService.create(principal, visitId, request));
    }

    @GetMapping("/visits/{id}/prescriptions")
    @PreAuthorize("hasAuthority('VISIT_READ')")
    public ResponseEntity<List<PrescriptionResponse>> listByVisit(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID visitId
    ) {
        return ResponseEntity.ok(prescriptionService.listByVisit(principal, visitId));
    }

    @PatchMapping("/prescriptions/{id}")
    @PreAuthorize("hasAuthority('VISIT_UPDATE')")
    public ResponseEntity<PrescriptionResponse> update(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID prescriptionId,
        @Valid @RequestBody PrescriptionPatchRequest request
    ) {
        return ResponseEntity.ok(prescriptionService.update(principal, prescriptionId, request));
    }
}

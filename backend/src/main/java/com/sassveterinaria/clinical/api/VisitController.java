package com.sassveterinaria.clinical.api;

import com.sassveterinaria.clinical.dto.VisitCreateRequest;
import com.sassveterinaria.clinical.dto.VisitPatchRequest;
import com.sassveterinaria.clinical.dto.VisitReopenRequest;
import com.sassveterinaria.clinical.dto.VisitResponse;
import com.sassveterinaria.clinical.service.VisitService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VISIT_CREATE')")
    public ResponseEntity<VisitResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody VisitCreateRequest request
    ) {
        return ResponseEntity.ok(visitService.create(principal, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VISIT_READ')")
    public ResponseEntity<VisitResponse> get(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID visitId
    ) {
        return ResponseEntity.ok(visitService.get(principal, visitId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('VISIT_UPDATE')")
    public ResponseEntity<VisitResponse> update(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID visitId,
        @Valid @RequestBody VisitPatchRequest request
    ) {
        return ResponseEntity.ok(visitService.update(principal, visitId, request));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('VISIT_CLOSE')")
    public ResponseEntity<VisitResponse> close(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID visitId
    ) {
        return ResponseEntity.ok(visitService.close(principal, visitId));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAuthority('VISIT_REOPEN')")
    public ResponseEntity<VisitResponse> reopen(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID visitId,
        @RequestBody(required = false) VisitReopenRequest request
    ) {
        String reason = request == null ? null : request.reason();
        return ResponseEntity.ok(visitService.reopen(principal, visitId, reason));
    }
}

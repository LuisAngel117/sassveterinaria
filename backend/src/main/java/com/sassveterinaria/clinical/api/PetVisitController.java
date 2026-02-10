package com.sassveterinaria.clinical.api;

import com.sassveterinaria.clinical.dto.VisitResponse;
import com.sassveterinaria.clinical.service.VisitService;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pets")
public class PetVisitController {

    private final VisitService visitService;

    public PetVisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping("/{petId}/visits")
    @PreAuthorize("hasAuthority('VISIT_READ')")
    public ResponseEntity<List<VisitResponse>> listByPet(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("petId") UUID petId,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return ResponseEntity.ok(visitService.listByPet(principal, petId, status, from, to));
    }
}

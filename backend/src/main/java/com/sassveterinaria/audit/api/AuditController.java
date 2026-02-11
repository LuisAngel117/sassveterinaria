package com.sassveterinaria.audit.api;

import com.sassveterinaria.audit.dto.AuditEventsPageResponse;
import com.sassveterinaria.audit.service.AuditService;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/events")
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    public ResponseEntity<AuditEventsPageResponse> events(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
        @RequestParam(value = "action", required = false) String action,
        @RequestParam(value = "entityType", required = false) String entityType,
        @RequestParam(value = "entityId", required = false) UUID entityId,
        @RequestParam(value = "actorUserId", required = false) UUID actorUserId,
        @RequestParam(value = "actorUsername", required = false) String actorUsername,
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "size", required = false) Integer size
    ) {
        return ResponseEntity.ok(
            auditService.search(principal, from, to, action, entityType, entityId, actorUserId, actorUsername, page, size)
        );
    }
}

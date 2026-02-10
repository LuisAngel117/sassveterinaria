package com.sassveterinaria.appointment.api;

import com.sassveterinaria.appointment.dto.RoomBlockCreateRequest;
import com.sassveterinaria.appointment.dto.RoomBlockResponse;
import com.sassveterinaria.appointment.service.RoomBlockService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/room-blocks")
public class RoomBlockController {

    private final RoomBlockService roomBlockService;

    public RoomBlockController(RoomBlockService roomBlockService) {
        this.roomBlockService = roomBlockService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BRANCH_MANAGE')")
    public ResponseEntity<RoomBlockResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody RoomBlockCreateRequest request
    ) {
        return ResponseEntity.ok(roomBlockService.create(principal, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('APPT_READ')")
    public ResponseEntity<List<RoomBlockResponse>> list(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
        @RequestParam(value = "roomId", required = false) UUID roomId
    ) {
        return ResponseEntity.ok(roomBlockService.list(principal, from, to, roomId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BRANCH_MANAGE')")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID roomBlockId
    ) {
        roomBlockService.delete(principal, roomBlockId);
        return ResponseEntity.noContent().build();
    }
}

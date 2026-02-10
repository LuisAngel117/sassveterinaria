package com.sassveterinaria.appointment.api;

import com.sassveterinaria.appointment.dto.RoomCreateRequest;
import com.sassveterinaria.appointment.dto.RoomResponse;
import com.sassveterinaria.appointment.service.RoomService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BRANCH_MANAGE')")
    public ResponseEntity<RoomResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody RoomCreateRequest request
    ) {
        return ResponseEntity.ok(roomService.create(principal, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('APPT_READ')")
    public ResponseEntity<List<RoomResponse>> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(roomService.list(principal));
    }
}

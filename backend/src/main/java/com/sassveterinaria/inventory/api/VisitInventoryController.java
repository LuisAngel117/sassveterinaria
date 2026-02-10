package com.sassveterinaria.inventory.api;

import com.sassveterinaria.inventory.dto.InventoryConsumeRequest;
import com.sassveterinaria.inventory.dto.InventoryConsumeResponse;
import com.sassveterinaria.inventory.service.InventoryService;
import com.sassveterinaria.security.AuthPrincipal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/visits")
public class VisitInventoryController {

    private final InventoryService inventoryService;

    public VisitInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/{visitId}/inventory/consume")
    @PreAuthorize("hasAuthority('STOCK_MOVE_CREATE')")
    public ResponseEntity<InventoryConsumeResponse> consume(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("visitId") UUID visitId,
        @RequestBody InventoryConsumeRequest request
    ) {
        return ResponseEntity.ok(inventoryService.consumeByVisit(principal, visitId, request));
    }
}

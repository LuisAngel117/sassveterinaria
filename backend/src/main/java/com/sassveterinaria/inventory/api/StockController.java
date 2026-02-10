package com.sassveterinaria.inventory.api;

import com.sassveterinaria.inventory.dto.LowStockResponse;
import com.sassveterinaria.inventory.dto.StockMovementCreateRequest;
import com.sassveterinaria.inventory.dto.StockMovementResponse;
import com.sassveterinaria.inventory.service.InventoryService;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/v1/stock")
public class StockController {

    private final InventoryService inventoryService;

    public StockController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/movements")
    @PreAuthorize("hasAnyAuthority('STOCK_MOVE_CREATE', 'STOCK_ADJUST')")
    public ResponseEntity<StockMovementResponse> createMovement(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestBody StockMovementCreateRequest request
    ) {
        return ResponseEntity.ok(inventoryService.createMovement(principal, request));
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public ResponseEntity<List<StockMovementResponse>> listMovements(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam(value = "productId", required = false) UUID productId,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
        @RequestParam(value = "type", required = false) String type
    ) {
        return ResponseEntity.ok(inventoryService.listMovements(principal, productId, from, to, type));
    }

    @GetMapping("/low")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public ResponseEntity<List<LowStockResponse>> listLowStock(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(inventoryService.listLowStock(principal));
    }
}

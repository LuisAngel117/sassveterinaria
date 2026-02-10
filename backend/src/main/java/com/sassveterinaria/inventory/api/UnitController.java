package com.sassveterinaria.inventory.api;

import com.sassveterinaria.inventory.dto.UnitResponse;
import com.sassveterinaria.inventory.service.InventoryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/units")
public class UnitController {

    private final InventoryService inventoryService;

    public UnitController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<UnitResponse>> list() {
        return ResponseEntity.ok(inventoryService.listUnits());
    }
}

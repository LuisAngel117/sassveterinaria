package com.sassveterinaria.inventory.api;

import com.sassveterinaria.inventory.dto.ServiceBomItemResponse;
import com.sassveterinaria.inventory.dto.ServiceBomReplaceRequest;
import com.sassveterinaria.inventory.service.InventoryService;
import com.sassveterinaria.security.AuthPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceBomController {

    private final InventoryService inventoryService;

    public ServiceBomController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{serviceId}/bom")
    @PreAuthorize("hasAuthority('SERVICE_READ')")
    public ResponseEntity<List<ServiceBomItemResponse>> getBom(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("serviceId") UUID serviceId
    ) {
        return ResponseEntity.ok(inventoryService.getServiceBom(principal, serviceId));
    }

    @PutMapping("/{serviceId}/bom")
    @PreAuthorize("hasAuthority('SERVICE_UPDATE')")
    public ResponseEntity<List<ServiceBomItemResponse>> replaceBom(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("serviceId") UUID serviceId,
        @RequestBody ServiceBomReplaceRequest request
    ) {
        return ResponseEntity.ok(inventoryService.replaceServiceBom(principal, serviceId, request));
    }
}

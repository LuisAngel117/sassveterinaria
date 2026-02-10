package com.sassveterinaria.appointment.api;

import com.sassveterinaria.appointment.dto.ServiceCreateRequest;
import com.sassveterinaria.appointment.dto.ServicePatchRequest;
import com.sassveterinaria.appointment.dto.ServiceResponse;
import com.sassveterinaria.appointment.service.ServiceCatalogService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SERVICE_CREATE')")
    public ResponseEntity<ServiceResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody ServiceCreateRequest request
    ) {
        return ResponseEntity.ok(serviceCatalogService.create(principal, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SERVICE_READ')")
    public ResponseEntity<List<ServiceResponse>> list(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "q", required = false) String q
    ) {
        return ResponseEntity.ok(serviceCatalogService.list(principal, active, q));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SERVICE_READ')")
    public ResponseEntity<ServiceResponse> get(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID serviceId
    ) {
        return ResponseEntity.ok(serviceCatalogService.get(principal, serviceId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SERVICE_UPDATE')")
    public ResponseEntity<ServiceResponse> update(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID serviceId,
        @Valid @RequestBody ServicePatchRequest request
    ) {
        return ResponseEntity.ok(serviceCatalogService.update(principal, serviceId, request));
    }
}

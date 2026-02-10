package com.sassveterinaria.appointment.api;

import com.sassveterinaria.appointment.dto.ServiceResponse;
import com.sassveterinaria.appointment.service.ServiceCatalogService;
import com.sassveterinaria.security.AuthPrincipal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('APPT_READ')")
    public ResponseEntity<List<ServiceResponse>> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(serviceCatalogService.list(principal));
    }
}

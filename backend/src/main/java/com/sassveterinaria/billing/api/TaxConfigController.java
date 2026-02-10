package com.sassveterinaria.billing.api;

import com.sassveterinaria.billing.dto.TaxConfigResponse;
import com.sassveterinaria.billing.dto.TaxConfigUpdateRequest;
import com.sassveterinaria.billing.service.TaxConfigService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config/tax")
public class TaxConfigController {

    private final TaxConfigService taxConfigService;

    public TaxConfigController(TaxConfigService taxConfigService) {
        this.taxConfigService = taxConfigService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONFIG_TAX_READ')")
    public ResponseEntity<TaxConfigResponse> get(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(taxConfigService.get(principal));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('CONFIG_TAX_UPDATE')")
    public ResponseEntity<TaxConfigResponse> update(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody TaxConfigUpdateRequest request
    ) {
        return ResponseEntity.ok(taxConfigService.update(principal, request));
    }
}

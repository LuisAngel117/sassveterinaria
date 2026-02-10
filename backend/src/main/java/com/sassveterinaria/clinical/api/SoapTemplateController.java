package com.sassveterinaria.clinical.api;

import com.sassveterinaria.clinical.dto.SoapTemplateCreateRequest;
import com.sassveterinaria.clinical.dto.SoapTemplatePatchRequest;
import com.sassveterinaria.clinical.dto.SoapTemplateResponse;
import com.sassveterinaria.clinical.service.SoapTemplateService;
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
@RequestMapping("/api/v1/soap-templates")
public class SoapTemplateController {

    private final SoapTemplateService soapTemplateService;

    public SoapTemplateController(SoapTemplateService soapTemplateService) {
        this.soapTemplateService = soapTemplateService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BRANCH_MANAGE')")
    public ResponseEntity<SoapTemplateResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody SoapTemplateCreateRequest request
    ) {
        return ResponseEntity.ok(soapTemplateService.create(principal, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VISIT_READ')")
    public ResponseEntity<List<SoapTemplateResponse>> listByService(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("serviceId") UUID serviceId
    ) {
        return ResponseEntity.ok(soapTemplateService.listByService(principal, serviceId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VISIT_READ')")
    public ResponseEntity<SoapTemplateResponse> get(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID templateId
    ) {
        return ResponseEntity.ok(soapTemplateService.get(principal, templateId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('BRANCH_MANAGE')")
    public ResponseEntity<SoapTemplateResponse> update(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID templateId,
        @Valid @RequestBody SoapTemplatePatchRequest request
    ) {
        return ResponseEntity.ok(soapTemplateService.update(principal, templateId, request));
    }
}

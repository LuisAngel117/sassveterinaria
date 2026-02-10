package com.sassveterinaria.billing.api;

import com.sassveterinaria.billing.dto.InvoiceDetailResponse;
import com.sassveterinaria.billing.dto.InvoiceItemPatchRequest;
import com.sassveterinaria.billing.service.BillingService;
import com.sassveterinaria.security.AuthPrincipal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoice-items")
public class InvoiceItemController {

    private final BillingService billingService;

    public InvoiceItemController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PatchMapping("/{itemId}")
    @PreAuthorize("hasAuthority('INVOICE_UPDATE')")
    public ResponseEntity<InvoiceDetailResponse> patchItem(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("itemId") UUID itemId,
        @RequestBody InvoiceItemPatchRequest request
    ) {
        return ResponseEntity.ok(billingService.patchItem(principal, itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAuthority('INVOICE_UPDATE')")
    public ResponseEntity<InvoiceDetailResponse> deleteItem(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("itemId") UUID itemId
    ) {
        return ResponseEntity.ok(billingService.deleteItem(principal, itemId));
    }
}

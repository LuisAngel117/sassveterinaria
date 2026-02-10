package com.sassveterinaria.billing.api;

import com.sassveterinaria.billing.dto.InvoiceCreateItemRequest;
import com.sassveterinaria.billing.dto.InvoiceCreateRequest;
import com.sassveterinaria.billing.dto.InvoiceDetailResponse;
import com.sassveterinaria.billing.dto.InvoicePatchRequest;
import com.sassveterinaria.billing.dto.InvoicePaymentCreateRequest;
import com.sassveterinaria.billing.dto.InvoicePaymentResponse;
import com.sassveterinaria.billing.dto.InvoiceResponse;
import com.sassveterinaria.billing.dto.InvoiceVoidRequest;
import com.sassveterinaria.billing.service.BillingService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/v1")
public class InvoiceController {

    private final BillingService billingService;

    public InvoiceController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/visits/{visitId}/invoices")
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    public ResponseEntity<InvoiceDetailResponse> createFromVisit(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("visitId") UUID visitId,
        @Valid @RequestBody InvoiceCreateRequest request
    ) {
        return ResponseEntity.ok(billingService.createFromVisit(principal, visitId, request));
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ResponseEntity<InvoiceDetailResponse> getById(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID invoiceId
    ) {
        return ResponseEntity.ok(billingService.getById(principal, invoiceId));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ResponseEntity<List<InvoiceResponse>> list(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
        @RequestParam(value = "q", required = false) String q
    ) {
        return ResponseEntity.ok(billingService.list(principal, status, from, to, q));
    }

    @PatchMapping("/invoices/{id}")
    @PreAuthorize("hasAuthority('INVOICE_UPDATE')")
    public ResponseEntity<InvoiceResponse> patchInvoice(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID invoiceId,
        @RequestBody InvoicePatchRequest request
    ) {
        return ResponseEntity.ok(billingService.patchInvoice(principal, invoiceId, request));
    }

    @PostMapping("/invoices/{id}/void")
    @PreAuthorize("hasAuthority('INVOICE_VOID')")
    public ResponseEntity<InvoiceResponse> voidInvoice(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID invoiceId,
        @Valid @RequestBody InvoiceVoidRequest request
    ) {
        return ResponseEntity.ok(billingService.voidInvoice(principal, invoiceId, request.reason()));
    }

    @PostMapping("/invoices/{id}/items")
    @PreAuthorize("hasAuthority('INVOICE_UPDATE')")
    public ResponseEntity<InvoiceDetailResponse> addItem(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID invoiceId,
        @Valid @RequestBody InvoiceCreateItemRequest request
    ) {
        return ResponseEntity.ok(billingService.addItem(principal, invoiceId, request));
    }

    @GetMapping("/invoices/{id}/payments")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ResponseEntity<List<InvoicePaymentResponse>> listPayments(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID invoiceId
    ) {
        return ResponseEntity.ok(billingService.listPayments(principal, invoiceId));
    }

    @PostMapping("/invoices/{id}/payments")
    @PreAuthorize("hasAuthority('INVOICE_PAY')")
    public ResponseEntity<InvoicePaymentResponse> addPayment(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID invoiceId,
        @Valid @RequestBody InvoicePaymentCreateRequest request
    ) {
        return ResponseEntity.ok(billingService.addPayment(principal, invoiceId, request));
    }

    @GetMapping("/invoices/{id}/export.csv")
    @PreAuthorize("hasAuthority('INVOICE_EXPORT')")
    public ResponseEntity<byte[]> exportCsv(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID invoiceId
    ) {
        byte[] body = billingService.exportInvoiceCsv(principal, invoiceId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename("invoice-" + invoiceId + ".csv", StandardCharsets.UTF_8)
            .build());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/invoices/{id}/export.pdf")
    @PreAuthorize("hasAuthority('INVOICE_EXPORT')")
    public ResponseEntity<byte[]> exportPdf(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID invoiceId
    ) {
        byte[] body = billingService.exportInvoicePdf(principal, invoiceId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename("invoice-" + invoiceId + ".pdf", StandardCharsets.UTF_8)
            .build());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/visits/{id}/instructions.pdf")
    @PreAuthorize("hasAuthority('VISIT_READ')")
    public ResponseEntity<byte[]> exportVisitInstructions(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID visitId
    ) {
        byte[] body = billingService.exportVisitInstructionsPdf(principal, visitId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename("visit-instructions-" + visitId + ".pdf", StandardCharsets.UTF_8)
            .build());
        return ResponseEntity.ok().headers(headers).body(body);
    }
}

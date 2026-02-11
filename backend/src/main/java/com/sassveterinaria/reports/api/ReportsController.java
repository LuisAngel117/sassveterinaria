package com.sassveterinaria.reports.api;

import com.sassveterinaria.reports.dto.AppointmentsReportResponse;
import com.sassveterinaria.reports.dto.DashboardResponse;
import com.sassveterinaria.reports.dto.FrequentReportResponse;
import com.sassveterinaria.reports.dto.InventoryConsumptionReportResponse;
import com.sassveterinaria.reports.dto.ReportExportPayload;
import com.sassveterinaria.reports.dto.SalesReportResponse;
import com.sassveterinaria.reports.dto.TopServicesReportResponse;
import com.sassveterinaria.reports.service.ReportsService;
import com.sassveterinaria.security.AuthPrincipal;
import com.sassveterinaria.security.RateLimitService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ReportsController {

    private final ReportsService reportsService;
    private final RateLimitService rateLimitService;

    public ReportsController(ReportsService reportsService, RateLimitService rateLimitService) {
        this.reportsService = reportsService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/reports/appointments")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<AppointmentsReportResponse> appointments(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "roomId", required = false) UUID roomId,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "groupBy", required = false) String groupBy,
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "size", required = false) Integer size
    ) {
        enforceReportsLimit(principal);
        return ResponseEntity.ok(reportsService.appointments(principal, from, to, roomId, status, groupBy, page, size));
    }

    @GetMapping("/reports/sales")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<SalesReportResponse> sales(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "groupBy", required = false) String groupBy,
        @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
        @RequestParam(value = "status", required = false) String status
    ) {
        enforceReportsLimit(principal);
        return ResponseEntity.ok(reportsService.sales(principal, from, to, groupBy, paymentMethod, status));
    }

    @GetMapping("/reports/top-services")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<TopServicesReportResponse> topServices(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "metric", required = false) String metric,
        @RequestParam(value = "limit", required = false) Integer limit
    ) {
        enforceReportsLimit(principal);
        return ResponseEntity.ok(reportsService.topServices(principal, from, to, metric, limit));
    }

    @GetMapping("/reports/inventory-consumption")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<InventoryConsumptionReportResponse> inventoryConsumption(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "productId", required = false) UUID productId,
        @RequestParam(value = "groupBy", required = false) String groupBy
    ) {
        enforceReportsLimit(principal);
        return ResponseEntity.ok(reportsService.inventoryConsumption(principal, from, to, productId, groupBy));
    }

    @GetMapping("/reports/frequent")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<FrequentReportResponse> frequent(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "limit", required = false) Integer limit,
        @RequestParam(value = "dimension", required = false) String dimension
    ) {
        enforceReportsLimit(principal);
        return ResponseEntity.ok(reportsService.frequent(principal, from, to, limit, dimension));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<DashboardResponse> dashboard(@AuthenticationPrincipal AuthPrincipal principal) {
        enforceReportsLimit(principal);
        return ResponseEntity.ok(reportsService.dashboard(principal));
    }

    @GetMapping("/reports/appointments/export.csv")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportAppointmentsCsv(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "roomId", required = false) UUID roomId,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "groupBy", required = false) String groupBy
    ) {
        enforceReportsLimit(principal);
        return csvResponse(reportsService.exportAppointmentsCsv(principal, from, to, roomId, status, groupBy));
    }

    @GetMapping("/reports/sales/export.csv")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportSalesCsv(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "groupBy", required = false) String groupBy,
        @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
        @RequestParam(value = "status", required = false) String status
    ) {
        enforceReportsLimit(principal);
        return csvResponse(reportsService.exportSalesCsv(principal, from, to, groupBy, paymentMethod, status));
    }

    @GetMapping("/reports/top-services/export.csv")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportTopServicesCsv(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "metric", required = false) String metric,
        @RequestParam(value = "limit", required = false) Integer limit
    ) {
        enforceReportsLimit(principal);
        return csvResponse(reportsService.exportTopServicesCsv(principal, from, to, metric, limit));
    }

    @GetMapping("/reports/inventory-consumption/export.csv")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportInventoryConsumptionCsv(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "productId", required = false) UUID productId,
        @RequestParam(value = "groupBy", required = false) String groupBy
    ) {
        enforceReportsLimit(principal);
        return csvResponse(reportsService.exportInventoryConsumptionCsv(principal, from, to, productId, groupBy));
    }

    @GetMapping("/reports/frequent/export.csv")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportFrequentCsv(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "limit", required = false) Integer limit,
        @RequestParam(value = "dimension", required = false) String dimension
    ) {
        enforceReportsLimit(principal);
        return csvResponse(reportsService.exportFrequentCsv(principal, from, to, limit, dimension));
    }

    @GetMapping("/reports/appointments/export.pdf")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportAppointmentsPdf(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "roomId", required = false) UUID roomId,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "groupBy", required = false) String groupBy
    ) {
        enforceReportsLimit(principal);
        return pdfResponse(reportsService.exportAppointmentsPdf(principal, from, to, roomId, status, groupBy));
    }

    @GetMapping("/reports/sales/export.pdf")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportSalesPdf(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "groupBy", required = false) String groupBy,
        @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
        @RequestParam(value = "status", required = false) String status
    ) {
        enforceReportsLimit(principal);
        return pdfResponse(reportsService.exportSalesPdf(principal, from, to, groupBy, paymentMethod, status));
    }

    @GetMapping("/reports/top-services/export.pdf")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportTopServicesPdf(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "metric", required = false) String metric,
        @RequestParam(value = "limit", required = false) Integer limit
    ) {
        enforceReportsLimit(principal);
        return pdfResponse(reportsService.exportTopServicesPdf(principal, from, to, metric, limit));
    }

    @GetMapping("/reports/inventory-consumption/export.pdf")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportInventoryConsumptionPdf(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "productId", required = false) UUID productId,
        @RequestParam(value = "groupBy", required = false) String groupBy
    ) {
        enforceReportsLimit(principal);
        return pdfResponse(reportsService.exportInventoryConsumptionPdf(principal, from, to, productId, groupBy));
    }

    @GetMapping("/reports/frequent/export.pdf")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportFrequentPdf(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam(value = "limit", required = false) Integer limit,
        @RequestParam(value = "dimension", required = false) String dimension
    ) {
        enforceReportsLimit(principal);
        return pdfResponse(reportsService.exportFrequentPdf(principal, from, to, limit, dimension));
    }

    private void enforceReportsLimit(AuthPrincipal principal) {
        rateLimitService.checkReports(principal);
    }

    private ResponseEntity<byte[]> csvResponse(ReportExportPayload payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(payload.filename(), StandardCharsets.UTF_8)
            .build());
        return ResponseEntity.ok().headers(headers).body(payload.content());
    }

    private ResponseEntity<byte[]> pdfResponse(ReportExportPayload payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(payload.filename(), StandardCharsets.UTF_8)
            .build());
        return ResponseEntity.ok().headers(headers).body(payload.content());
    }
}

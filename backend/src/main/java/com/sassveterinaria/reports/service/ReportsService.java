package com.sassveterinaria.reports.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sassveterinaria.appointment.domain.AppointmentEntity;
import com.sassveterinaria.appointment.domain.AppointmentStatus;
import com.sassveterinaria.appointment.domain.RoomEntity;
import com.sassveterinaria.appointment.domain.ServiceEntity;
import com.sassveterinaria.appointment.repo.AppointmentRepository;
import com.sassveterinaria.appointment.repo.RoomRepository;
import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.billing.domain.InvoiceEntity;
import com.sassveterinaria.billing.domain.InvoiceItemEntity;
import com.sassveterinaria.billing.domain.InvoiceItemType;
import com.sassveterinaria.billing.domain.InvoiceStatus;
import com.sassveterinaria.billing.domain.PaymentMethod;
import com.sassveterinaria.billing.domain.InvoicePaymentEntity;
import com.sassveterinaria.billing.repo.InvoiceItemRepository;
import com.sassveterinaria.billing.repo.InvoicePaymentRepository;
import com.sassveterinaria.billing.repo.InvoiceRepository;
import com.sassveterinaria.clinical.domain.VisitEntity;
import com.sassveterinaria.clinical.domain.VisitStatus;
import com.sassveterinaria.clinical.repo.VisitRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.crm.domain.ClientEntity;
import com.sassveterinaria.crm.domain.PetEntity;
import com.sassveterinaria.crm.repo.ClientRepository;
import com.sassveterinaria.crm.repo.PetRepository;
import com.sassveterinaria.inventory.domain.ProductEntity;
import com.sassveterinaria.inventory.domain.StockMovementEntity;
import com.sassveterinaria.inventory.repo.ProductRepository;
import com.sassveterinaria.inventory.repo.StockMovementRepository;
import com.sassveterinaria.reports.dto.AppointmentsReportItem;
import com.sassveterinaria.reports.dto.AppointmentsReportResponse;
import com.sassveterinaria.reports.dto.DashboardResponse;
import com.sassveterinaria.reports.dto.FrequentDimension;
import com.sassveterinaria.reports.dto.FrequentItem;
import com.sassveterinaria.reports.dto.FrequentReportResponse;
import com.sassveterinaria.reports.dto.InventoryConsumptionItem;
import com.sassveterinaria.reports.dto.InventoryConsumptionReportResponse;
import com.sassveterinaria.reports.dto.InventoryGroupBy;
import com.sassveterinaria.reports.dto.ReportBreakdownItem;
import com.sassveterinaria.reports.dto.ReportExportPayload;
import com.sassveterinaria.reports.dto.ReportGroupBy;
import com.sassveterinaria.reports.dto.ReportSeriesPoint;
import com.sassveterinaria.reports.dto.SalesReportResponse;
import com.sassveterinaria.reports.dto.TopServiceItem;
import com.sassveterinaria.reports.dto.TopServiceMetric;
import com.sassveterinaria.reports.dto.TopServicesReportResponse;
import com.sassveterinaria.security.AuthPrincipal;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportsService {

    private static final ZoneId GUAYAQUIL_ZONE = ZoneId.of("America/Guayaquil");

    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;
    private final ClientRepository clientRepository;
    private final PetRepository petRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final VisitRepository visitRepository;
    private final ServiceRepository serviceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public ReportsService(
        AppointmentRepository appointmentRepository,
        RoomRepository roomRepository,
        ClientRepository clientRepository,
        PetRepository petRepository,
        InvoiceRepository invoiceRepository,
        InvoiceItemRepository invoiceItemRepository,
        InvoicePaymentRepository invoicePaymentRepository,
        VisitRepository visitRepository,
        ServiceRepository serviceRepository,
        StockMovementRepository stockMovementRepository,
        ProductRepository productRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.roomRepository = roomRepository;
        this.clientRepository = clientRepository;
        this.petRepository = petRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.invoicePaymentRepository = invoicePaymentRepository;
        this.visitRepository = visitRepository;
        this.serviceRepository = serviceRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public AppointmentsReportResponse appointments(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        UUID roomId,
        String statusRaw,
        String groupByRaw,
        Integer pageRaw,
        Integer sizeRaw
    ) {
        OffsetDateTime from = parseRequiredDateTime("from", fromRaw);
        OffsetDateTime to = parseRequiredDateTime("to", toRaw);
        validateDateRange(from, to);
        ReportGroupBy groupBy = parseGroupBy(groupByRaw);
        int page = normalizePage(pageRaw);
        int size = normalizePageSize(sizeRaw);

        String status = null;
        if (statusRaw != null && !statusRaw.isBlank()) {
            AppointmentStatus parsedStatus = AppointmentStatus.fromValue(statusRaw);
            if (parsedStatus == null) {
                throw validation("status invalido para appointments.");
            }
            status = parsedStatus.name();
        }

        List<AppointmentEntity> appointments = appointmentRepository.search(
            principal.getBranchId(),
            from,
            to,
            roomId,
            status
        );
        Map<UUID, String> roomNameById = roomNameMap(principal.getBranchId(), appointments.stream()
            .map(AppointmentEntity::getRoomId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<UUID, String> clientNameById = clientNameMap(principal.getBranchId(), appointments.stream()
            .map(AppointmentEntity::getClientId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<UUID, String> petNameById = petNameMap(principal.getBranchId(), appointments.stream()
            .map(AppointmentEntity::getPetId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new)));

        List<AppointmentsReportItem> allItems = appointments.stream()
            .map(appointment -> new AppointmentsReportItem(
                appointment.getId(),
                appointment.getStartsAt(),
                appointment.getEndsAt(),
                appointment.getStatus(),
                appointment.getRoomId(),
                roomNameById.get(appointment.getRoomId()),
                appointment.getClientId(),
                clientNameById.get(appointment.getClientId()),
                appointment.getPetId(),
                petNameById.get(appointment.getPetId())
            ))
            .toList();

        List<ReportSeriesPoint> series = buildAppointmentSeries(allItems, groupBy);
        long total = allItems.size();
        int start = Math.min(page * size, allItems.size());
        int end = Math.min(start + size, allItems.size());
        List<AppointmentsReportItem> pagedItems = allItems.subList(start, end);

        return new AppointmentsReportResponse(
            from,
            to,
            groupBy.name().toLowerCase(),
            total,
            series,
            pagedItems,
            page,
            size,
            total
        );
    }

    @Transactional(readOnly = true)
    public SalesReportResponse sales(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        String groupByRaw,
        String paymentMethodRaw,
        String statusRaw
    ) {
        OffsetDateTime from = parseRequiredDateTime("from", fromRaw);
        OffsetDateTime to = parseRequiredDateTime("to", toRaw);
        validateDateRange(from, to);
        ReportGroupBy groupBy = parseGroupBy(groupByRaw);
        String status = parseInvoiceStatus(statusRaw);
        PaymentMethod paymentMethod = parsePaymentMethod(paymentMethodRaw);

        List<InvoiceEntity> invoices = invoiceRepository.search(principal.getBranchId(), status, from, to, null);
        if (invoices.isEmpty()) {
            return new SalesReportResponse(
                from,
                to,
                groupBy.name().toLowerCase(),
                moneyZero(),
                moneyZero(),
                moneyZero(),
                List.of(),
                List.of()
            );
        }

        List<UUID> invoiceIds = invoices.stream().map(InvoiceEntity::getId).toList();
        List<InvoicePaymentEntity> payments = invoicePaymentRepository.findByBranchIdAndInvoiceIdIn(principal.getBranchId(), invoiceIds);
        Map<UUID, List<InvoicePaymentEntity>> paymentsByInvoice = payments.stream()
            .collect(Collectors.groupingBy(InvoicePaymentEntity::getInvoiceId));

        List<InvoiceEntity> filteredInvoices = invoices;
        if (paymentMethod != null) {
            filteredInvoices = invoices.stream()
                .filter(invoice -> paymentsByInvoice.getOrDefault(invoice.getId(), List.of()).stream()
                    .anyMatch(payment -> paymentMethod.name().equals(payment.getMethod())))
                .toList();
        }

        BigDecimal totalFacturado = filteredInvoices.stream()
            .map(InvoiceEntity::getTotal)
            .reduce(moneyZero(), BigDecimal::add);
        BigDecimal totalImpuesto = filteredInvoices.stream()
            .map(InvoiceEntity::getTaxAmount)
            .reduce(moneyZero(), BigDecimal::add);
        BigDecimal totalCobrado = filteredInvoices.stream()
            .map(invoice -> paidForInvoice(paymentsByInvoice.getOrDefault(invoice.getId(), List.of()), paymentMethod))
            .reduce(moneyZero(), BigDecimal::add);

        List<ReportSeriesPoint> series = buildSalesSeries(filteredInvoices, groupBy);
        List<ReportBreakdownItem> breakdown = buildPaymentBreakdown(filteredInvoices, paymentsByInvoice, paymentMethod);

        return new SalesReportResponse(
            from,
            to,
            groupBy.name().toLowerCase(),
            roundMoney(totalFacturado),
            roundMoney(totalImpuesto),
            roundMoney(totalCobrado),
            series,
            breakdown
        );
    }

    @Transactional(readOnly = true)
    public TopServicesReportResponse topServices(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        String metricRaw,
        Integer limitRaw
    ) {
        OffsetDateTime from = parseRequiredDateTime("from", fromRaw);
        OffsetDateTime to = parseRequiredDateTime("to", toRaw);
        validateDateRange(from, to);
        TopServiceMetric metric = parseTopServiceMetric(metricRaw);
        int limit = normalizeLimit(limitRaw);

        List<InvoiceEntity> invoices = invoiceRepository.search(principal.getBranchId(), null, from, to, null);
        List<TopServiceItem> items = buildTopServicesFromInvoices(principal, invoices);
        if (items.isEmpty()) {
            items = buildTopServicesFromVisits(principal, from, to);
        }

        Comparator<TopServiceItem> comparator = metric == TopServiceMetric.COUNT
            ? Comparator.comparing(TopServiceItem::count).reversed()
            : Comparator.comparing(TopServiceItem::revenue).reversed();
        List<TopServiceItem> sorted = items.stream().sorted(comparator).toList();
        List<TopServiceItem> limited = sorted.stream().limit(limit).toList();

        BigDecimal totalValue = metric == TopServiceMetric.COUNT
            ? sorted.stream().map(TopServiceItem::count).reduce(BigDecimal.ZERO, BigDecimal::add)
            : sorted.stream().map(TopServiceItem::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TopServicesReportResponse(
            from,
            to,
            metric.name().toLowerCase(),
            limit,
            roundMoney(totalValue),
            limited
        );
    }

    @Transactional(readOnly = true)
    public InventoryConsumptionReportResponse inventoryConsumption(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        UUID productId,
        String groupByRaw
    ) {
        OffsetDateTime from = parseRequiredDateTime("from", fromRaw);
        OffsetDateTime to = parseRequiredDateTime("to", toRaw);
        validateDateRange(from, to);
        InventoryGroupBy groupBy = parseInventoryGroupBy(groupByRaw);

        List<StockMovementEntity> movements = stockMovementRepository.findForConsumptionReport(
            principal.getBranchId(),
            from,
            to,
            List.of("CONSUME", "OUT"),
            productId
        );

        List<InventoryConsumptionItem> items = groupBy == InventoryGroupBy.PRODUCT
            ? buildInventoryByProduct(principal, movements)
            : buildInventoryByService(principal, movements);

        BigDecimal totalQty = items.stream().map(InventoryConsumptionItem::qty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = items.stream().map(InventoryConsumptionItem::totalCost).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new InventoryConsumptionReportResponse(
            from,
            to,
            groupBy.name().toLowerCase(),
            qtyScale(totalQty),
            costScale(totalCost),
            items
        );
    }

    @Transactional(readOnly = true)
    public FrequentReportResponse frequent(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        Integer limitRaw,
        String dimensionRaw
    ) {
        OffsetDateTime from = parseRequiredDateTime("from", fromRaw);
        OffsetDateTime to = parseRequiredDateTime("to", toRaw);
        validateDateRange(from, to);
        int limit = normalizeLimit(limitRaw);
        FrequentDimension dimension = parseFrequentDimension(dimensionRaw);

        List<VisitEntity> visits = visitRepository.findByBranchIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            principal.getBranchId(),
            from,
            to
        );

        List<FrequentItem> items = dimension == FrequentDimension.PET
            ? buildFrequentPets(principal, visits, limit)
            : buildFrequentClients(principal, visits, limit);

        return new FrequentReportResponse(
            from,
            to,
            dimension.name().toLowerCase(),
            limit,
            visits.size(),
            items
        );
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(AuthPrincipal principal) {
        OffsetDateTime startToday = startOfToday();
        OffsetDateTime endToday = startToday.plusDays(1);

        long todayAppointmentsCount = appointmentRepository.search(
            principal.getBranchId(),
            startToday,
            endToday,
            null,
            null
        ).size();
        long todayInProgressVisitsCount = visitRepository.countByBranchIdAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            principal.getBranchId(),
            VisitStatus.OPEN.name(),
            startToday,
            endToday
        );
        BigDecimal todaySalesTotal = invoiceRepository.search(principal.getBranchId(), null, startToday, endToday, null)
            .stream()
            .filter(invoice -> !InvoiceStatus.VOID.name().equals(invoice.getStatus()))
            .map(InvoiceEntity::getTotal)
            .reduce(moneyZero(), BigDecimal::add);
        long lowStockCount = productRepository.search(principal.getBranchId(), null, Boolean.TRUE, Boolean.TRUE).size();

        OffsetDateTime weekStart = OffsetDateTime.now(GUAYAQUIL_ZONE)
            .with(java.time.DayOfWeek.MONDAY)
            .toLocalDate()
            .atStartOfDay()
            .atZone(GUAYAQUIL_ZONE)
            .toOffsetDateTime();
        OffsetDateTime weekEnd = weekStart.plusDays(7);
        List<TopServiceItem> topServices = topServices(
            principal,
            weekStart.toString(),
            weekEnd.toString(),
            TopServiceMetric.COUNT.name(),
            5
        ).items();

        return new DashboardResponse(
            todayAppointmentsCount,
            todayInProgressVisitsCount,
            roundMoney(todaySalesTotal),
            lowStockCount,
            topServices
        );
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportAppointmentsCsv(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        UUID roomId,
        String statusRaw,
        String groupByRaw
    ) {
        AppointmentsReportResponse report = appointments(principal, fromRaw, toRaw, roomId, statusRaw, groupByRaw, 0, 1000);
        StringBuilder csv = new StringBuilder();
        appendMetadata(csv, "appointments", report.from(), report.to(), principal.getBranchId());
        csv.append("appointmentId,start,end,status,roomId,roomName,clientId,clientName,petId,petName\n");
        for (AppointmentsReportItem item : report.items()) {
            csv.append(item.appointmentId()).append(',')
                .append(item.start()).append(',')
                .append(item.end()).append(',')
                .append(item.status()).append(',')
                .append(safe(item.roomId())).append(',')
                .append(csvSafe(item.roomName())).append(',')
                .append(safe(item.clientId())).append(',')
                .append(csvSafe(item.clientName())).append(',')
                .append(safe(item.petId())).append(',')
                .append(csvSafe(item.petName())).append('\n');
        }
        return new ReportExportPayload(buildFilename("appointments", report.from(), report.to(), "csv"), csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportSalesCsv(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        String groupByRaw,
        String paymentMethodRaw,
        String statusRaw
    ) {
        SalesReportResponse report = sales(principal, fromRaw, toRaw, groupByRaw, paymentMethodRaw, statusRaw);
        StringBuilder csv = new StringBuilder();
        appendMetadata(csv, "sales", report.from(), report.to(), principal.getBranchId());
        csv.append("totalFacturado,totalImpuesto,totalCobrado\n")
            .append(report.totalFacturado()).append(',')
            .append(report.totalImpuesto()).append(',')
            .append(report.totalCobrado()).append('\n');
        csv.append('\n');
        csv.append("seriesKey,seriesValue\n");
        for (ReportSeriesPoint point : report.series()) {
            csv.append(point.key()).append(',').append(point.value()).append('\n');
        }
        csv.append('\n');
        csv.append("paymentMethod,amount\n");
        for (ReportBreakdownItem item : report.breakdown()) {
            csv.append(item.key()).append(',').append(item.value()).append('\n');
        }
        return new ReportExportPayload(buildFilename("sales", report.from(), report.to(), "csv"), csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportTopServicesCsv(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        String metricRaw,
        Integer limitRaw
    ) {
        TopServicesReportResponse report = topServices(principal, fromRaw, toRaw, metricRaw, limitRaw);
        StringBuilder csv = new StringBuilder();
        appendMetadata(csv, "top-services", report.from(), report.to(), principal.getBranchId());
        csv.append("serviceId,serviceName,count,revenue\n");
        for (TopServiceItem item : report.items()) {
            csv.append(safe(item.serviceId())).append(',')
                .append(csvSafe(item.serviceName())).append(',')
                .append(item.count()).append(',')
                .append(item.revenue()).append('\n');
        }
        return new ReportExportPayload(buildFilename("top-services", report.from(), report.to(), "csv"), csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportInventoryConsumptionCsv(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        UUID productId,
        String groupByRaw
    ) {
        InventoryConsumptionReportResponse report = inventoryConsumption(principal, fromRaw, toRaw, productId, groupByRaw);
        StringBuilder csv = new StringBuilder();
        appendMetadata(csv, "inventory-consumption", report.from(), report.to(), principal.getBranchId());
        csv.append("groupBy,entityId,label,qty,totalCost\n");
        for (InventoryConsumptionItem item : report.items()) {
            csv.append(item.groupBy()).append(',')
                .append(safe(item.entityId())).append(',')
                .append(csvSafe(item.label())).append(',')
                .append(item.qty()).append(',')
                .append(item.totalCost()).append('\n');
        }
        return new ReportExportPayload(
            buildFilename("inventory-consumption", report.from(), report.to(), "csv"),
            csv.toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportFrequentCsv(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        Integer limitRaw,
        String dimensionRaw
    ) {
        FrequentReportResponse report = frequent(principal, fromRaw, toRaw, limitRaw, dimensionRaw);
        StringBuilder csv = new StringBuilder();
        appendMetadata(csv, "frequent", report.from(), report.to(), principal.getBranchId());
        csv.append("entityId,displayName,count\n");
        for (FrequentItem item : report.items()) {
            csv.append(item.entityId()).append(',')
                .append(csvSafe(item.displayName())).append(',')
                .append(item.count()).append('\n');
        }
        return new ReportExportPayload(buildFilename("frequent", report.from(), report.to(), "csv"), csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportAppointmentsPdf(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        UUID roomId,
        String statusRaw,
        String groupByRaw
    ) {
        AppointmentsReportResponse report = appointments(principal, fromRaw, toRaw, roomId, statusRaw, groupByRaw, 0, 1000);
        byte[] content = writePdf(document -> {
            document.add(new Paragraph("Reporte de Citas"));
            document.add(new Paragraph("Rango: " + report.from() + " a " + report.to()));
            document.add(new Paragraph("Total: " + report.totalAppointments()));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(5);
            table.addCell("Inicio");
            table.addCell("Fin");
            table.addCell("Estado");
            table.addCell("Sala");
            table.addCell("Paciente");
            for (AppointmentsReportItem item : report.items()) {
                table.addCell(String.valueOf(item.start()));
                table.addCell(String.valueOf(item.end()));
                table.addCell(item.status());
                table.addCell(item.roomName() == null ? "-" : item.roomName());
                table.addCell(item.petName() == null ? "-" : item.petName());
            }
            document.add(table);
        });
        return new ReportExportPayload(buildFilename("appointments", report.from(), report.to(), "pdf"), content);
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportSalesPdf(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        String groupByRaw,
        String paymentMethodRaw,
        String statusRaw
    ) {
        SalesReportResponse report = sales(principal, fromRaw, toRaw, groupByRaw, paymentMethodRaw, statusRaw);
        byte[] content = writePdf(document -> {
            document.add(new Paragraph("Reporte de Ventas"));
            document.add(new Paragraph("Rango: " + report.from() + " a " + report.to()));
            document.add(new Paragraph("Facturado: " + report.totalFacturado()));
            document.add(new Paragraph("Impuesto: " + report.totalImpuesto()));
            document.add(new Paragraph("Cobrado: " + report.totalCobrado()));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(2);
            table.addCell("Metodo");
            table.addCell("Monto");
            for (ReportBreakdownItem item : report.breakdown()) {
                table.addCell(item.key());
                table.addCell(String.valueOf(item.value()));
            }
            document.add(table);
        });
        return new ReportExportPayload(buildFilename("sales", report.from(), report.to(), "pdf"), content);
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportTopServicesPdf(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        String metricRaw,
        Integer limitRaw
    ) {
        TopServicesReportResponse report = topServices(principal, fromRaw, toRaw, metricRaw, limitRaw);
        byte[] content = writePdf(document -> {
            document.add(new Paragraph("Reporte Top Servicios"));
            document.add(new Paragraph("Rango: " + report.from() + " a " + report.to()));
            document.add(new Paragraph("Metrica: " + report.metric()));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(4);
            table.addCell("Servicio");
            table.addCell("Cantidad");
            table.addCell("Ingresos");
            table.addCell("Id");
            for (TopServiceItem item : report.items()) {
                table.addCell(item.serviceName() == null ? "-" : item.serviceName());
                table.addCell(String.valueOf(item.count()));
                table.addCell(String.valueOf(item.revenue()));
                table.addCell(safe(item.serviceId()));
            }
            document.add(table);
        });
        return new ReportExportPayload(buildFilename("top-services", report.from(), report.to(), "pdf"), content);
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportInventoryConsumptionPdf(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        UUID productId,
        String groupByRaw
    ) {
        InventoryConsumptionReportResponse report = inventoryConsumption(principal, fromRaw, toRaw, productId, groupByRaw);
        byte[] content = writePdf(document -> {
            document.add(new Paragraph("Reporte Consumo Inventario"));
            document.add(new Paragraph("Rango: " + report.from() + " a " + report.to()));
            document.add(new Paragraph("Cantidad total: " + report.totalQty()));
            document.add(new Paragraph("Costo total: " + report.totalCost()));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(4);
            table.addCell("Agrupador");
            table.addCell("Entidad");
            table.addCell("Qty");
            table.addCell("Costo");
            for (InventoryConsumptionItem item : report.items()) {
                table.addCell(item.groupBy());
                table.addCell(item.label() == null ? "-" : item.label());
                table.addCell(String.valueOf(item.qty()));
                table.addCell(String.valueOf(item.totalCost()));
            }
            document.add(table);
        });
        return new ReportExportPayload(buildFilename("inventory-consumption", report.from(), report.to(), "pdf"), content);
    }

    @Transactional(readOnly = true)
    public ReportExportPayload exportFrequentPdf(
        AuthPrincipal principal,
        String fromRaw,
        String toRaw,
        Integer limitRaw,
        String dimensionRaw
    ) {
        FrequentReportResponse report = frequent(principal, fromRaw, toRaw, limitRaw, dimensionRaw);
        byte[] content = writePdf(document -> {
            document.add(new Paragraph("Reporte Frecuentes"));
            document.add(new Paragraph("Rango: " + report.from() + " a " + report.to()));
            document.add(new Paragraph("Dimension: " + report.dimension()));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(3);
            table.addCell("Id");
            table.addCell("Nombre");
            table.addCell("Conteo");
            for (FrequentItem item : report.items()) {
                table.addCell(String.valueOf(item.entityId()));
                table.addCell(item.displayName());
                table.addCell(String.valueOf(item.count()));
            }
            document.add(table);
        });
        return new ReportExportPayload(buildFilename("frequent", report.from(), report.to(), "pdf"), content);
    }

    private List<ReportSeriesPoint> buildAppointmentSeries(List<AppointmentsReportItem> items, ReportGroupBy groupBy) {
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        for (AppointmentsReportItem item : items) {
            String key = groupByKey(item.start(), groupBy);
            data.merge(key, BigDecimal.ONE, BigDecimal::add);
        }
        return data.entrySet().stream()
            .map(entry -> new ReportSeriesPoint(entry.getKey(), entry.getValue()))
            .toList();
    }

    private List<ReportSeriesPoint> buildSalesSeries(List<InvoiceEntity> invoices, ReportGroupBy groupBy) {
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        for (InvoiceEntity invoice : invoices) {
            String key = groupByKey(invoice.getCreatedAt(), groupBy);
            data.merge(key, invoice.getTotal(), BigDecimal::add);
        }
        return data.entrySet().stream()
            .map(entry -> new ReportSeriesPoint(entry.getKey(), roundMoney(entry.getValue())))
            .toList();
    }

    private List<ReportBreakdownItem> buildPaymentBreakdown(
        List<InvoiceEntity> invoices,
        Map<UUID, List<InvoicePaymentEntity>> paymentsByInvoice,
        PaymentMethod paymentMethodFilter
    ) {
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        for (InvoiceEntity invoice : invoices) {
            for (InvoicePaymentEntity payment : paymentsByInvoice.getOrDefault(invoice.getId(), List.of())) {
                if (paymentMethodFilter != null && !paymentMethodFilter.name().equals(payment.getMethod())) {
                    continue;
                }
                data.merge(payment.getMethod(), payment.getAmount(), BigDecimal::add);
            }
        }
        return data.entrySet().stream()
            .map(entry -> new ReportBreakdownItem(entry.getKey(), roundMoney(entry.getValue())))
            .toList();
    }

    private List<TopServiceItem> buildTopServicesFromInvoices(AuthPrincipal principal, List<InvoiceEntity> invoices) {
        if (invoices.isEmpty()) {
            return List.of();
        }
        List<UUID> invoiceIds = invoices.stream().map(InvoiceEntity::getId).toList();
        List<InvoiceItemEntity> items = invoiceItemRepository.findByBranchIdAndInvoiceIdInAndItemType(
            principal.getBranchId(),
            invoiceIds,
            InvoiceItemType.SERVICE.name()
        );
        if (items.isEmpty()) {
            return List.of();
        }

        Map<UUID, BigDecimal> countByService = new LinkedHashMap<>();
        Map<UUID, BigDecimal> revenueByService = new LinkedHashMap<>();
        Map<UUID, String> descriptionByService = new LinkedHashMap<>();

        for (InvoiceItemEntity item : items) {
            countByService.merge(item.getItemId(), item.getQty(), BigDecimal::add);
            revenueByService.merge(item.getItemId(), item.getLineTotal(), BigDecimal::add);
            descriptionByService.putIfAbsent(item.getItemId(), item.getDescription());
        }

        Map<UUID, String> serviceNameById = serviceRepository.findByBranchIdAndIdIn(
            principal.getBranchId(),
            new ArrayList<>(countByService.keySet())
        ).stream().collect(Collectors.toMap(ServiceEntity::getId, ServiceEntity::getName));

        return countByService.keySet().stream()
            .map(serviceId -> new TopServiceItem(
                serviceId,
                serviceNameById.getOrDefault(serviceId, descriptionByService.get(serviceId)),
                qtyScale(countByService.getOrDefault(serviceId, BigDecimal.ZERO)),
                roundMoney(revenueByService.getOrDefault(serviceId, BigDecimal.ZERO))
            ))
            .toList();
    }

    private List<TopServiceItem> buildTopServicesFromVisits(AuthPrincipal principal, OffsetDateTime from, OffsetDateTime to) {
        List<VisitEntity> visits = visitRepository.findByBranchIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            principal.getBranchId(),
            from,
            to
        );
        if (visits.isEmpty()) {
            return List.of();
        }

        Map<UUID, BigDecimal> countByService = new LinkedHashMap<>();
        for (VisitEntity visit : visits) {
            countByService.merge(visit.getServiceId(), BigDecimal.ONE, BigDecimal::add);
        }

        Map<UUID, String> serviceNameById = serviceRepository.findByBranchIdAndIdIn(
            principal.getBranchId(),
            new ArrayList<>(countByService.keySet())
        ).stream().collect(Collectors.toMap(ServiceEntity::getId, ServiceEntity::getName));

        return countByService.entrySet().stream()
            .map(entry -> new TopServiceItem(
                entry.getKey(),
                serviceNameById.get(entry.getKey()),
                qtyScale(entry.getValue()),
                moneyZero()
            ))
            .toList();
    }

    private List<InventoryConsumptionItem> buildInventoryByProduct(AuthPrincipal principal, List<StockMovementEntity> movements) {
        if (movements.isEmpty()) {
            return List.of();
        }
        Map<UUID, BigDecimal> qtyByProduct = new LinkedHashMap<>();
        Map<UUID, BigDecimal> costByProduct = new LinkedHashMap<>();
        for (StockMovementEntity movement : movements) {
            qtyByProduct.merge(movement.getProductId(), movement.getQty(), BigDecimal::add);
            costByProduct.merge(movement.getProductId(), movement.getTotalCost(), BigDecimal::add);
        }
        Map<UUID, String> productNameById = productRepository.findByBranchIdAndIdIn(
            principal.getBranchId(),
            new ArrayList<>(qtyByProduct.keySet())
        ).stream().collect(Collectors.toMap(ProductEntity::getId, ProductEntity::getName));

        return qtyByProduct.keySet().stream()
            .map(productId -> new InventoryConsumptionItem(
                "product",
                productId,
                productNameById.get(productId),
                qtyScale(qtyByProduct.getOrDefault(productId, BigDecimal.ZERO)),
                costScale(costByProduct.getOrDefault(productId, BigDecimal.ZERO))
            ))
            .sorted(Comparator.comparing(InventoryConsumptionItem::totalCost).reversed())
            .toList();
    }

    private List<InventoryConsumptionItem> buildInventoryByService(AuthPrincipal principal, List<StockMovementEntity> movements) {
        if (movements.isEmpty()) {
            return List.of();
        }

        List<UUID> visitIds = movements.stream()
            .map(StockMovementEntity::getVisitId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<UUID, VisitEntity> visitById = visitRepository.findByBranchIdAndIdIn(principal.getBranchId(), visitIds)
            .stream()
            .collect(Collectors.toMap(VisitEntity::getId, visit -> visit));

        Map<UUID, BigDecimal> qtyByService = new LinkedHashMap<>();
        Map<UUID, BigDecimal> costByService = new LinkedHashMap<>();
        for (StockMovementEntity movement : movements) {
            VisitEntity visit = movement.getVisitId() == null ? null : visitById.get(movement.getVisitId());
            UUID serviceId = visit == null ? null : visit.getServiceId();
            qtyByService.merge(serviceId, movement.getQty(), BigDecimal::add);
            costByService.merge(serviceId, movement.getTotalCost(), BigDecimal::add);
        }

        Map<UUID, String> serviceNameById = serviceRepository.findByBranchIdAndIdIn(
            principal.getBranchId(),
            qtyByService.keySet().stream().filter(Objects::nonNull).toList()
        ).stream().collect(Collectors.toMap(ServiceEntity::getId, ServiceEntity::getName));

        return qtyByService.keySet().stream()
            .map(serviceId -> new InventoryConsumptionItem(
                "service",
                serviceId,
                serviceId == null ? "Sin servicio" : serviceNameById.get(serviceId),
                qtyScale(qtyByService.getOrDefault(serviceId, BigDecimal.ZERO)),
                costScale(costByService.getOrDefault(serviceId, BigDecimal.ZERO))
            ))
            .sorted(Comparator.comparing(InventoryConsumptionItem::totalCost).reversed())
            .toList();
    }

    private List<FrequentItem> buildFrequentPets(AuthPrincipal principal, List<VisitEntity> visits, int limit) {
        if (visits.isEmpty()) {
            return List.of();
        }
        Map<UUID, Long> countByPet = visits.stream()
            .collect(Collectors.groupingBy(VisitEntity::getPetId, Collectors.counting()));
        Map<UUID, String> petNameById = petNameMap(principal.getBranchId(), countByPet.keySet());
        return countByPet.entrySet().stream()
            .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> new FrequentItem(entry.getKey(), petNameById.get(entry.getKey()), entry.getValue()))
            .toList();
    }

    private List<FrequentItem> buildFrequentClients(AuthPrincipal principal, List<VisitEntity> visits, int limit) {
        if (visits.isEmpty()) {
            return List.of();
        }
        List<UUID> petIds = visits.stream().map(VisitEntity::getPetId).distinct().toList();
        Map<UUID, PetEntity> petById = petRepository.findByBranchIdAndIdIn(principal.getBranchId(), petIds).stream()
            .collect(Collectors.toMap(PetEntity::getId, pet -> pet));

        Map<UUID, Long> countByClient = new LinkedHashMap<>();
        for (VisitEntity visit : visits) {
            PetEntity pet = petById.get(visit.getPetId());
            if (pet == null) {
                continue;
            }
            countByClient.merge(pet.getClientId(), 1L, Long::sum);
        }

        Map<UUID, String> clientNameById = clientNameMap(principal.getBranchId(), countByClient.keySet());
        return countByClient.entrySet().stream()
            .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> new FrequentItem(entry.getKey(), clientNameById.get(entry.getKey()), entry.getValue()))
            .toList();
    }

    private Map<UUID, String> roomNameMap(UUID branchId, java.util.Collection<UUID> roomIds) {
        if (roomIds.isEmpty()) {
            return Map.of();
        }
        List<RoomEntity> rooms = roomRepository.findByBranchIdAndIdIn(branchId, new ArrayList<>(roomIds));
        return rooms.stream().collect(Collectors.toMap(RoomEntity::getId, RoomEntity::getName));
    }

    private Map<UUID, String> clientNameMap(UUID branchId, java.util.Collection<UUID> clientIds) {
        if (clientIds.isEmpty()) {
            return Map.of();
        }
        List<ClientEntity> clients = clientRepository.findByBranchIdAndIdIn(branchId, new ArrayList<>(clientIds));
        return clients.stream().collect(Collectors.toMap(ClientEntity::getId, ClientEntity::getFullName));
    }

    private Map<UUID, String> petNameMap(UUID branchId, java.util.Collection<UUID> petIds) {
        if (petIds.isEmpty()) {
            return Map.of();
        }
        List<PetEntity> pets = petRepository.findByBranchIdAndIdIn(branchId, new ArrayList<>(petIds));
        return pets.stream().collect(Collectors.toMap(PetEntity::getId, PetEntity::getName));
    }

    private String parseInvoiceStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        InvoiceStatus status = InvoiceStatus.fromValue(value);
        if (status == null) {
            throw validation("status invalido para sales.");
        }
        return status.name();
    }

    private PaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        PaymentMethod method = PaymentMethod.fromValue(value);
        if (method == null) {
            throw validation("paymentMethod invalido.");
        }
        return method;
    }

    private ReportGroupBy parseGroupBy(String value) {
        ReportGroupBy groupBy = ReportGroupBy.fromValue(value);
        if (groupBy == null) {
            throw validation("groupBy invalido. Valores permitidos: day, week.");
        }
        return groupBy;
    }

    private TopServiceMetric parseTopServiceMetric(String value) {
        TopServiceMetric metric = TopServiceMetric.fromValue(value);
        if (metric == null) {
            throw validation("metric invalido. Valores permitidos: count, revenue.");
        }
        return metric;
    }

    private InventoryGroupBy parseInventoryGroupBy(String value) {
        InventoryGroupBy groupBy = InventoryGroupBy.fromValue(value);
        if (groupBy == null) {
            throw validation("groupBy invalido. Valores permitidos: product, service.");
        }
        return groupBy;
    }

    private FrequentDimension parseFrequentDimension(String value) {
        FrequentDimension dimension = FrequentDimension.fromValue(value);
        if (dimension == null) {
            throw validation("dimension invalida. Valores permitidos: client, pet.");
        }
        return dimension;
    }

    private int normalizeLimit(Integer limitRaw) {
        int limit = limitRaw == null ? 10 : limitRaw;
        if (limit < 1 || limit > 100) {
            throw validation("limit debe estar entre 1 y 100.");
        }
        return limit;
    }

    private int normalizePage(Integer pageRaw) {
        int page = pageRaw == null ? 0 : pageRaw;
        if (page < 0) {
            throw validation("page debe ser >= 0.");
        }
        return page;
    }

    private int normalizePageSize(Integer sizeRaw) {
        int size = sizeRaw == null ? 50 : sizeRaw;
        if (size < 1 || size > 500) {
            throw validation("size debe estar entre 1 y 500.");
        }
        return size;
    }

    private OffsetDateTime parseRequiredDateTime(String field, String value) {
        if (value == null || value.isBlank()) {
            throw validation(field + " es requerido.");
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(value);
                return localDateTime.atZone(GUAYAQUIL_ZONE).toOffsetDateTime();
            } catch (DateTimeParseException ex) {
                throw validation(field + " debe ser una fecha ISO-8601 valida.");
            }
        }
    }

    private void validateDateRange(OffsetDateTime from, OffsetDateTime to) {
        if (from.isAfter(to)) {
            throw validation("from no puede ser mayor que to.");
        }
    }

    private BigDecimal paidForInvoice(List<InvoicePaymentEntity> payments, PaymentMethod paymentMethodFilter) {
        BigDecimal total = moneyZero();
        for (InvoicePaymentEntity payment : payments) {
            if (paymentMethodFilter != null && !paymentMethodFilter.name().equals(payment.getMethod())) {
                continue;
            }
            total = total.add(payment.getAmount());
        }
        return roundMoney(total);
    }

    private String groupByKey(OffsetDateTime dateTime, ReportGroupBy groupBy) {
        OffsetDateTime normalized = dateTime.atZoneSameInstant(GUAYAQUIL_ZONE).toOffsetDateTime();
        if (groupBy == ReportGroupBy.WEEK) {
            int week = normalized.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int year = normalized.get(IsoFields.WEEK_BASED_YEAR);
            return year + "-W" + String.format("%02d", week);
        }
        return normalized.toLocalDate().toString();
    }

    private OffsetDateTime startOfToday() {
        LocalDate today = LocalDate.now(GUAYAQUIL_ZONE);
        return today.atStartOfDay().atZone(GUAYAQUIL_ZONE).toOffsetDateTime();
    }

    private void appendMetadata(StringBuilder csv, String reportName, OffsetDateTime from, OffsetDateTime to, UUID branchId) {
        csv.append("report,").append(reportName).append('\n');
        csv.append("branch,").append(branchId).append('\n');
        csv.append("from,").append(from).append('\n');
        csv.append("to,").append(to).append('\n');
        csv.append("generatedAt,").append(OffsetDateTime.now(GUAYAQUIL_ZONE)).append('\n');
        csv.append('\n');
    }

    private String buildFilename(String reportName, OffsetDateTime from, OffsetDateTime to, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String fromPart = formatter.format(from.atZoneSameInstant(GUAYAQUIL_ZONE));
        String toPart = formatter.format(to.atZoneSameInstant(GUAYAQUIL_ZONE));
        return "report-" + reportName + "-" + fromPart + "-" + toPart + "." + extension;
    }

    private byte[] writePdf(PdfWriterCallback callback) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            callback.write(document);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException ex) {
            throw new ApiProblemException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "https://sassveterinaria.local/errors/report-pdf-export-failed",
                "Report PDF export failed",
                "No se pudo generar el PDF del reporte.",
                "REPORT_PDF_EXPORT_FAILED"
            );
        }
    }

    private String csvSafe(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replace("\"", "\"\"");
        if (cleaned.contains(",") || cleaned.contains("\n")) {
            return "\"" + cleaned + "\"";
        }
        return cleaned;
    }

    private String safe(UUID value) {
        return value == null ? "" : value.toString();
    }

    private BigDecimal moneyZero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal roundMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal qtyScale(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal costScale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private ApiProblemException validation(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/report-validation",
            "Report validation error",
            detail,
            "REPORT_VALIDATION_ERROR"
        );
    }

    @FunctionalInterface
    private interface PdfWriterCallback {
        void write(Document document) throws DocumentException;
    }
}

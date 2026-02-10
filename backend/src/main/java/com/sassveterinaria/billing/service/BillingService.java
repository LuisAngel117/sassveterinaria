package com.sassveterinaria.billing.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sassveterinaria.appointment.domain.ServiceEntity;
import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.audit.service.AuditService;
import com.sassveterinaria.billing.domain.InvoiceCounterEntity;
import com.sassveterinaria.billing.domain.InvoiceEntity;
import com.sassveterinaria.billing.domain.InvoiceItemEntity;
import com.sassveterinaria.billing.domain.InvoiceItemType;
import com.sassveterinaria.billing.domain.InvoicePaymentEntity;
import com.sassveterinaria.billing.domain.InvoiceStatus;
import com.sassveterinaria.billing.domain.PaymentMethod;
import com.sassveterinaria.billing.domain.TaxConfigEntity;
import com.sassveterinaria.billing.dto.InvoiceCreateItemRequest;
import com.sassveterinaria.billing.dto.InvoiceCreateRequest;
import com.sassveterinaria.billing.dto.InvoiceDetailResponse;
import com.sassveterinaria.billing.dto.InvoiceItemPatchRequest;
import com.sassveterinaria.billing.dto.InvoiceItemResponse;
import com.sassveterinaria.billing.dto.InvoicePatchRequest;
import com.sassveterinaria.billing.dto.InvoicePaymentCreateRequest;
import com.sassveterinaria.billing.dto.InvoicePaymentResponse;
import com.sassveterinaria.billing.dto.InvoiceResponse;
import com.sassveterinaria.billing.repo.InvoiceCounterRepository;
import com.sassveterinaria.billing.repo.InvoiceItemRepository;
import com.sassveterinaria.billing.repo.InvoicePaymentRepository;
import com.sassveterinaria.billing.repo.InvoiceRepository;
import com.sassveterinaria.clinical.domain.PrescriptionEntity;
import com.sassveterinaria.clinical.domain.VisitEntity;
import com.sassveterinaria.clinical.repo.PrescriptionRepository;
import com.sassveterinaria.clinical.service.VisitService;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.crm.domain.ClientEntity;
import com.sassveterinaria.crm.domain.PetEntity;
import com.sassveterinaria.crm.repo.ClientRepository;
import com.sassveterinaria.crm.repo.PetRepository;
import com.sassveterinaria.inventory.service.InventoryService;
import com.sassveterinaria.security.AuthPrincipal;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {

    private static final ZoneId GUAYAQUIL_ZONE = ZoneId.of("America/Guayaquil");

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final InvoiceCounterRepository invoiceCounterRepository;
    private final ServiceRepository serviceRepository;
    private final VisitService visitService;
    private final TaxConfigService taxConfigService;
    private final AuditService auditService;
    private final PetRepository petRepository;
    private final ClientRepository clientRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final InventoryService inventoryService;

    public BillingService(
        InvoiceRepository invoiceRepository,
        InvoiceItemRepository invoiceItemRepository,
        InvoicePaymentRepository invoicePaymentRepository,
        InvoiceCounterRepository invoiceCounterRepository,
        ServiceRepository serviceRepository,
        VisitService visitService,
        TaxConfigService taxConfigService,
        AuditService auditService,
        PetRepository petRepository,
        ClientRepository clientRepository,
        PrescriptionRepository prescriptionRepository,
        InventoryService inventoryService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.invoicePaymentRepository = invoicePaymentRepository;
        this.invoiceCounterRepository = invoiceCounterRepository;
        this.serviceRepository = serviceRepository;
        this.visitService = visitService;
        this.taxConfigService = taxConfigService;
        this.auditService = auditService;
        this.petRepository = petRepository;
        this.clientRepository = clientRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public InvoiceDetailResponse createFromVisit(AuthPrincipal principal, UUID visitId, InvoiceCreateRequest request) {
        VisitEntity visit = visitService.requireVisit(principal, visitId);
        TaxConfigEntity taxConfig = taxConfigService.requireOrCreate(principal);
        OffsetDateTime now = OffsetDateTime.now();

        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId(UUID.randomUUID());
        invoice.setBranchId(principal.getBranchId());
        invoice.setVisitId(visit.getId());
        invoice.setInvoiceNumber(nextInvoiceNumber(principal.getBranchId()));
        invoice.setStatus(InvoiceStatus.PENDING.name());
        invoice.setItemsSubtotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        invoice.setDiscountAmount(normalizeDiscount(request.discountAmount()));
        invoice.setTaxRate(taxConfig.getTaxRate());
        invoice.setTaxAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        invoice.setTotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        invoice.setVoidReason(null);
        invoice.setVoidedAt(null);
        invoice.setCreatedBy(principal.getUserId());
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);
        InvoiceEntity savedInvoice = invoiceRepository.save(invoice);

        List<InvoiceItemEntity> items = new ArrayList<>();
        Map<UUID, BigDecimal> projectedProductQty = new LinkedHashMap<>();
        for (InvoiceCreateItemRequest itemRequest : request.items()) {
            items.add(buildItemEntity(principal, savedInvoice, itemRequest, now, projectedProductQty));
        }
        invoiceItemRepository.saveAll(items);
        InvoiceEntity recalculated = recalculateAndSave(savedInvoice);
        return buildDetail(recalculated);
    }

    @Transactional(readOnly = true)
    public InvoiceDetailResponse getById(AuthPrincipal principal, UUID invoiceId) {
        return buildDetail(requireInvoice(principal, invoiceId));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> list(
        AuthPrincipal principal,
        String status,
        OffsetDateTime from,
        OffsetDateTime to,
        String q
    ) {
        String statusFilter = null;
        if (status != null && !status.isBlank()) {
            InvoiceStatus parsed = InvoiceStatus.fromValue(status);
            if (parsed == null) {
                throw validation("status invalido.");
            }
            statusFilter = parsed.name();
        }
        return invoiceRepository.search(principal.getBranchId(), statusFilter, from, to, normalizeText(q))
            .stream()
            .map(this::toInvoiceResponse)
            .toList();
    }

    @Transactional
    public InvoiceResponse patchInvoice(AuthPrincipal principal, UUID invoiceId, InvoicePatchRequest request) {
        InvoiceEntity invoice = requireInvoice(principal, invoiceId);
        ensurePending(invoice);
        Map<String, Object> before = snapshotInvoice(invoice);

        boolean sensitiveChange = false;
        if (request.discountAmount() != null) {
            BigDecimal newDiscount = normalizeDiscount(request.discountAmount());
            sensitiveChange = invoice.getDiscountAmount().compareTo(newDiscount) != 0;
            invoice.setDiscountAmount(newDiscount);
        }
        if (!sensitiveChange) {
            return toInvoiceResponse(invoice);
        }

        String reason = normalizeReason(request.reason());
        InvoiceEntity saved = recalculateAndSave(invoice);
        auditService.record(
            principal,
            "INVOICE_UPDATE",
            "invoice",
            saved.getId(),
            reason,
            before,
            snapshotInvoice(saved)
        );
        return toInvoiceResponse(saved);
    }

    @Transactional
    public InvoiceDetailResponse addItem(AuthPrincipal principal, UUID invoiceId, InvoiceCreateItemRequest request) {
        InvoiceEntity invoice = requireInvoice(principal, invoiceId);
        ensurePending(invoice);
        InvoiceItemEntity item = buildItemEntity(principal, invoice, request, OffsetDateTime.now(), null);
        invoiceItemRepository.save(item);
        InvoiceEntity saved = recalculateAndSave(invoice);
        return buildDetail(saved);
    }

    @Transactional
    public InvoiceDetailResponse patchItem(AuthPrincipal principal, UUID itemId, InvoiceItemPatchRequest request) {
        InvoiceItemEntity item = requireItem(principal, itemId);
        InvoiceEntity invoice = requireInvoice(principal, item.getInvoiceId());
        ensurePending(invoice);

        Map<String, Object> beforeInvoice = snapshotInvoice(invoice);
        Map<String, Object> before = snapshotItem(item);
        boolean sensitiveChange = false;
        if (request.description() != null) {
            item.setDescription(normalizeRequiredText(request.description(), 200, "description no puede ser vacio."));
        }
        if (request.qty() != null) {
            item.setQty(normalizeQty(request.qty()));
        }
        if (request.unitPrice() != null) {
            BigDecimal newUnitPrice = normalizeMoney(request.unitPrice(), "unitPrice debe ser >= 0.");
            sensitiveChange = sensitiveChange || item.getUnitPrice().compareTo(newUnitPrice) != 0;
            item.setUnitPrice(newUnitPrice);
        }
        if (request.discountAmount() != null) {
            BigDecimal newDiscount = normalizeDiscount(request.discountAmount());
            sensitiveChange = sensitiveChange || item.getDiscountAmount().compareTo(newDiscount) != 0;
            item.setDiscountAmount(newDiscount);
        }

        BigDecimal lineBase = lineBase(item.getQty(), item.getUnitPrice());
        if (item.getDiscountAmount().compareTo(lineBase) > 0) {
            throw validation("discountAmount del item no puede exceder qty * unitPrice.");
        }
        item.setLineTotal(roundCurrency(lineBase.subtract(item.getDiscountAmount())));

        boolean stockValidationRequired = InvoiceItemType.PRODUCT.name().equals(item.getItemType())
            && (request.qty() != null || Boolean.TRUE.equals(request.override()));
        if (stockValidationRequired) {
            BigDecimal otherQty = invoiceItemRepository.sumProductQtyExcludingItem(
                principal.getBranchId(),
                invoice.getId(),
                item.getItemId(),
                item.getId()
            );
            BigDecimal projectedQty = otherQty.add(item.getQty());
            inventoryService.validateInvoiceProductStock(
                principal,
                invoice.getId(),
                item.getItemId(),
                projectedQty,
                Boolean.TRUE.equals(request.override()),
                request.reason()
            );
        }

        String reason = null;
        if (sensitiveChange) {
            reason = normalizeReason(request.reason());
        }

        invoiceItemRepository.save(item);
        InvoiceEntity savedInvoice = recalculateAndSave(invoice);
        if (sensitiveChange) {
            Map<String, Object> after = snapshotItem(item);
            Map<String, Object> beforePayload = new LinkedHashMap<>();
            beforePayload.put("invoice", beforeInvoice);
            beforePayload.put("item", before);
            Map<String, Object> afterPayload = new LinkedHashMap<>();
            afterPayload.put("invoice", snapshotInvoice(savedInvoice));
            afterPayload.put("item", after);
            auditService.record(
                principal,
                "INVOICE_UPDATE",
                "invoice",
                savedInvoice.getId(),
                reason,
                beforePayload,
                afterPayload
            );
        }
        return buildDetail(savedInvoice);
    }

    @Transactional
    public InvoiceDetailResponse deleteItem(AuthPrincipal principal, UUID itemId) {
        InvoiceItemEntity item = requireItem(principal, itemId);
        InvoiceEntity invoice = requireInvoice(principal, item.getInvoiceId());
        ensurePending(invoice);
        invoiceItemRepository.delete(item);
        InvoiceEntity saved = recalculateAndSave(invoice);
        return buildDetail(saved);
    }

    @Transactional
    public InvoicePaymentResponse addPayment(AuthPrincipal principal, UUID invoiceId, InvoicePaymentCreateRequest request) {
        InvoiceEntity invoice = requireInvoice(principal, invoiceId);
        if (InvoiceStatus.fromValue(invoice.getStatus()) == InvoiceStatus.VOID) {
            throw conflict(
                "https://sassveterinaria.local/errors/invoice-void",
                "Invoice void",
                "La factura anulada no permite pagos.",
                "INVOICE_VOID"
            );
        }

        PaymentMethod method = parsePaymentMethod(request.method());
        BigDecimal amount = normalizePositiveMoney(request.amount(), "amount debe ser mayor a 0.");

        InvoicePaymentEntity payment = new InvoicePaymentEntity();
        payment.setId(UUID.randomUUID());
        payment.setBranchId(principal.getBranchId());
        payment.setInvoiceId(invoice.getId());
        payment.setMethod(method.name());
        payment.setAmount(amount);
        payment.setReference(normalizeOptionalText(request.reference(), 80));
        payment.setCreatedBy(principal.getUserId());
        payment.setCreatedAt(OffsetDateTime.now());
        InvoicePaymentEntity saved = invoicePaymentRepository.save(payment);

        recalculateAndSave(invoice);
        return toPaymentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoicePaymentResponse> listPayments(AuthPrincipal principal, UUID invoiceId) {
        requireInvoice(principal, invoiceId);
        return invoicePaymentRepository.findByBranchIdAndInvoiceIdOrderByCreatedAtAsc(principal.getBranchId(), invoiceId)
            .stream()
            .map(this::toPaymentResponse)
            .toList();
    }

    @Transactional
    public InvoiceResponse voidInvoice(AuthPrincipal principal, UUID invoiceId, String reason) {
        InvoiceEntity invoice = requireInvoice(principal, invoiceId);
        if (InvoiceStatus.fromValue(invoice.getStatus()) == InvoiceStatus.VOID) {
            throw conflict(
                "https://sassveterinaria.local/errors/invoice-already-void",
                "Invoice already void",
                "La factura ya esta anulada.",
                "INVOICE_ALREADY_VOID"
            );
        }

        String normalizedReason = normalizeReason(reason);
        Map<String, Object> before = snapshotInvoice(invoice);
        invoice.setStatus(InvoiceStatus.VOID.name());
        invoice.setVoidReason(normalizedReason);
        invoice.setVoidedAt(OffsetDateTime.now());
        invoice.setUpdatedAt(OffsetDateTime.now());
        InvoiceEntity saved = invoiceRepository.save(invoice);
        auditService.record(
            principal,
            "INVOICE_VOID",
            "invoice",
            saved.getId(),
            normalizedReason,
            before,
            snapshotInvoice(saved)
        );
        return toInvoiceResponse(saved);
    }

    @Transactional(readOnly = true)
    public byte[] exportInvoiceCsv(AuthPrincipal principal, UUID invoiceId) {
        InvoiceDetailResponse detail = getById(principal, invoiceId);
        StringBuilder csv = new StringBuilder();
        csv.append("invoiceNumber,status,itemsSubtotal,discountAmount,taxRate,taxAmount,total,paidTotal\n");
        csv.append(detail.invoice().invoiceNumber()).append(',')
            .append(detail.invoice().status()).append(',')
            .append(detail.invoice().itemsSubtotal()).append(',')
            .append(detail.invoice().discountAmount()).append(',')
            .append(detail.invoice().taxRate()).append(',')
            .append(detail.invoice().taxAmount()).append(',')
            .append(detail.invoice().total()).append(',')
            .append(detail.invoice().paidTotal()).append('\n');

        csv.append('\n');
        csv.append("itemType,description,qty,unitPrice,discountAmount,lineTotal\n");
        for (InvoiceItemResponse item : detail.items()) {
            csv.append(item.itemType()).append(',')
                .append(csvSafe(item.description())).append(',')
                .append(item.qty()).append(',')
                .append(item.unitPrice()).append(',')
                .append(item.discountAmount()).append(',')
                .append(item.lineTotal()).append('\n');
        }

        csv.append('\n');
        csv.append("paymentMethod,amount,reference,createdAt\n");
        for (InvoicePaymentResponse payment : detail.payments()) {
            csv.append(payment.method()).append(',')
                .append(payment.amount()).append(',')
                .append(csvSafe(payment.reference())).append(',')
                .append(payment.createdAt()).append('\n');
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportInvoicePdf(AuthPrincipal principal, UUID invoiceId) {
        InvoiceDetailResponse detail = getById(principal, invoiceId);
        return writePdf(document -> {
            document.add(new Paragraph("Factura " + detail.invoice().invoiceNumber()));
            document.add(new Paragraph("Estado: " + detail.invoice().status()));
            document.add(new Paragraph("Subtotal: " + detail.invoice().itemsSubtotal()));
            document.add(new Paragraph("Descuento: " + detail.invoice().discountAmount()));
            document.add(new Paragraph("IVA (" + detail.invoice().taxRate() + "): " + detail.invoice().taxAmount()));
            document.add(new Paragraph("Total: " + detail.invoice().total()));
            document.add(new Paragraph("Pagado: " + detail.invoice().paidTotal()));
            document.add(new Paragraph(" "));

            PdfPTable itemsTable = new PdfPTable(5);
            itemsTable.addCell("Tipo");
            itemsTable.addCell("Descripcion");
            itemsTable.addCell("Qty");
            itemsTable.addCell("P. Unit");
            itemsTable.addCell("Total");
            for (InvoiceItemResponse item : detail.items()) {
                itemsTable.addCell(item.itemType());
                itemsTable.addCell(item.description());
                itemsTable.addCell(String.valueOf(item.qty()));
                itemsTable.addCell(String.valueOf(item.unitPrice()));
                itemsTable.addCell(String.valueOf(item.lineTotal()));
            }
            document.add(itemsTable);

            document.add(new Paragraph(" "));
            PdfPTable paymentsTable = new PdfPTable(3);
            paymentsTable.addCell("Metodo");
            paymentsTable.addCell("Monto");
            paymentsTable.addCell("Referencia");
            for (InvoicePaymentResponse payment : detail.payments()) {
                paymentsTable.addCell(payment.method());
                paymentsTable.addCell(String.valueOf(payment.amount()));
                paymentsTable.addCell(payment.reference() == null ? "" : payment.reference());
            }
            document.add(paymentsTable);
        });
    }

    @Transactional(readOnly = true)
    public byte[] exportVisitInstructionsPdf(AuthPrincipal principal, UUID visitId) {
        VisitEntity visit = visitService.requireVisit(principal, visitId);
        PetEntity pet = petRepository.findByIdAndBranchId(visit.getPetId(), principal.getBranchId())
            .orElseThrow(() -> notFound(
                "https://sassveterinaria.local/errors/pet-not-found",
                "Pet not found",
                "No se encontro la mascota.",
                "PET_NOT_FOUND"
            ));
        ClientEntity client = clientRepository.findByIdAndBranchId(pet.getClientId(), principal.getBranchId())
            .orElseThrow(() -> notFound(
                "https://sassveterinaria.local/errors/client-not-found",
                "Client not found",
                "No se encontro el cliente.",
                "CLIENT_NOT_FOUND"
            ));
        List<PrescriptionEntity> prescriptions = prescriptionRepository
            .findByBranchIdAndVisitIdOrderByCreatedAtAsc(principal.getBranchId(), visitId);

        return writePdf(document -> {
            document.add(new Paragraph("Indicaciones de Atencion"));
            document.add(new Paragraph("Mascota: " + pet.getName() + " (" + pet.getSpecies() + ")"));
            document.add(new Paragraph("Propietario: " + client.getFullName()));
            document.add(new Paragraph("Diagnostico: " + safeText(visit.getADiagnosis())));
            document.add(new Paragraph("Tratamiento: " + safeText(visit.getPTreatment())));
            document.add(new Paragraph("Indicaciones: " + safeText(visit.getPInstructions())));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Prescripciones:"));

            PdfPTable table = new PdfPTable(6);
            table.addCell("Medicamento");
            table.addCell("Dosis");
            table.addCell("Unidad");
            table.addCell("Frecuencia");
            table.addCell("Duracion");
            table.addCell("Via");
            for (PrescriptionEntity rx : prescriptions) {
                table.addCell(rx.getMedication());
                table.addCell(rx.getDose());
                table.addCell(rx.getUnit());
                table.addCell(rx.getFrequency());
                table.addCell(rx.getDuration());
                table.addCell(rx.getRoute());
            }
            document.add(table);
        });
    }

    private InvoiceEntity recalculateAndSave(InvoiceEntity invoice) {
        List<InvoiceItemEntity> items = invoiceItemRepository.findByBranchIdAndInvoiceIdOrderByCreatedAtAsc(
            invoice.getBranchId(),
            invoice.getId()
        );
        BigDecimal itemsSubtotal = items.stream()
            .map(InvoiceItemEntity::getLineTotal)
            .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);

        BigDecimal discount = normalizeDiscount(invoice.getDiscountAmount());
        if (discount.compareTo(itemsSubtotal) > 0) {
            throw validation("discountAmount no puede exceder el subtotal de items.");
        }
        BigDecimal taxableBase = roundCurrency(itemsSubtotal.subtract(discount));
        BigDecimal taxAmount = roundCurrency(taxableBase.multiply(invoice.getTaxRate()));
        BigDecimal total = roundCurrency(taxableBase.add(taxAmount));

        invoice.setItemsSubtotal(itemsSubtotal);
        invoice.setDiscountAmount(discount);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotal(total);
        if (InvoiceStatus.fromValue(invoice.getStatus()) != InvoiceStatus.VOID) {
            BigDecimal paidTotal = invoicePaymentRepository.sumAmountByInvoice(invoice.getBranchId(), invoice.getId());
            invoice.setStatus(paidTotal.compareTo(total) >= 0 ? InvoiceStatus.PAID.name() : InvoiceStatus.PENDING.name());
        }
        invoice.setUpdatedAt(OffsetDateTime.now());
        return invoiceRepository.save(invoice);
    }

    private InvoiceItemEntity buildItemEntity(
        AuthPrincipal principal,
        InvoiceEntity invoice,
        InvoiceCreateItemRequest request,
        OffsetDateTime now,
        Map<UUID, BigDecimal> projectedProductQty
    ) {
        InvoiceItemType itemType = parseItemType(request.itemType());
        UUID itemId = request.itemId();

        BigDecimal qty = normalizeQty(request.qty());
        BigDecimal unitPrice;
        String description;

        if (itemType == InvoiceItemType.SERVICE) {
            ServiceEntity service = serviceRepository.findByIdAndBranchId(itemId, principal.getBranchId())
                .orElseThrow(() -> notFound(
                    "https://sassveterinaria.local/errors/service-not-found",
                    "Service not found",
                    "No se encontro el servicio.",
                    "SERVICE_NOT_FOUND"
                ));
            description = request.description() == null || request.description().isBlank()
                ? service.getName()
                : normalizeRequiredText(request.description(), 200, "description invalida.");
            unitPrice = request.unitPrice() == null
                ? service.getPriceBase()
                : normalizeMoney(request.unitPrice(), "unitPrice debe ser >= 0.");
        } else {
            String productName = inventoryService.requireProductNameForBilling(principal, itemId);
            description = request.description() == null || request.description().isBlank()
                ? productName
                : normalizeRequiredText(request.description(), 200, "description invalida.");
            unitPrice = normalizeMoney(request.unitPrice(), "unitPrice es requerido para PRODUCT y debe ser >= 0.");
            BigDecimal targetQty;
            if (projectedProductQty != null) {
                targetQty = projectedProductQty.merge(itemId, qty, BigDecimal::add);
            } else {
                BigDecimal currentQty = invoiceItemRepository.sumProductQty(principal.getBranchId(), invoice.getId(), itemId);
                targetQty = currentQty.add(qty);
            }
            inventoryService.validateInvoiceProductStock(
                principal,
                invoice.getId(),
                itemId,
                targetQty,
                Boolean.TRUE.equals(request.override()),
                request.reason()
            );
        }

        BigDecimal discount = normalizeDiscount(request.discountAmount());
        BigDecimal lineBase = lineBase(qty, unitPrice);
        if (discount.compareTo(lineBase) > 0) {
            throw validation("discountAmount del item no puede exceder qty * unitPrice.");
        }

        InvoiceItemEntity item = new InvoiceItemEntity();
        item.setId(UUID.randomUUID());
        item.setBranchId(invoice.getBranchId());
        item.setInvoiceId(invoice.getId());
        item.setItemType(itemType.name());
        item.setItemId(itemId);
        item.setDescription(description);
        item.setQty(qty);
        item.setUnitPrice(unitPrice);
        item.setDiscountAmount(discount);
        item.setLineTotal(roundCurrency(lineBase.subtract(discount)));
        item.setCreatedAt(now);
        return item;
    }

    private String nextInvoiceNumber(UUID branchId) {
        InvoiceCounterEntity counter = invoiceCounterRepository.findByBranchIdForUpdate(branchId).orElseGet(() -> {
            InvoiceCounterEntity created = new InvoiceCounterEntity();
            created.setBranchId(branchId);
            created.setNextNumber(1);
            return invoiceCounterRepository.saveAndFlush(created);
        });
        int currentNumber = counter.getNextNumber();
        counter.setNextNumber(currentNumber + 1);
        invoiceCounterRepository.save(counter);
        String period = DateTimeFormatter.ofPattern("yyyyMM").format(OffsetDateTime.now(GUAYAQUIL_ZONE));
        return String.format("INV-%s-%06d", period, currentNumber);
    }

    private InvoiceDetailResponse buildDetail(InvoiceEntity invoice) {
        List<InvoiceItemResponse> items = invoiceItemRepository
            .findByBranchIdAndInvoiceIdOrderByCreatedAtAsc(invoice.getBranchId(), invoice.getId())
            .stream()
            .map(this::toItemResponse)
            .toList();

        List<InvoicePaymentResponse> payments = invoicePaymentRepository
            .findByBranchIdAndInvoiceIdOrderByCreatedAtAsc(invoice.getBranchId(), invoice.getId())
            .stream()
            .map(this::toPaymentResponse)
            .toList();

        BigDecimal paidTotal = payments.stream()
            .map(InvoicePaymentResponse::amount)
            .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);

        return new InvoiceDetailResponse(toInvoiceResponse(invoice, paidTotal), items, payments);
    }

    private InvoiceEntity requireInvoice(AuthPrincipal principal, UUID invoiceId) {
        return invoiceRepository.findByIdAndBranchId(invoiceId, principal.getBranchId())
            .orElseThrow(() -> notFound(
                "https://sassveterinaria.local/errors/invoice-not-found",
                "Invoice not found",
                "No se encontro la factura.",
                "INVOICE_NOT_FOUND"
            ));
    }

    private InvoiceItemEntity requireItem(AuthPrincipal principal, UUID itemId) {
        return invoiceItemRepository.findByIdAndBranchId(itemId, principal.getBranchId())
            .orElseThrow(() -> notFound(
                "https://sassveterinaria.local/errors/invoice-item-not-found",
                "Invoice item not found",
                "No se encontro el item de factura.",
                "INVOICE_ITEM_NOT_FOUND"
            ));
    }

    private void ensurePending(InvoiceEntity invoice) {
        if (InvoiceStatus.fromValue(invoice.getStatus()) != InvoiceStatus.PENDING) {
            throw conflict(
                "https://sassveterinaria.local/errors/invoice-not-pending",
                "Invoice not pending",
                "Solo se puede modificar una factura en estado PENDING.",
                "INVOICE_NOT_PENDING"
            );
        }
    }

    private ApiProblemException notFound(String type, String title, String detail, String code) {
        return new ApiProblemException(HttpStatus.NOT_FOUND, type, title, detail, code);
    }

    private ApiProblemException conflict(String type, String title, String detail, String code) {
        return new ApiProblemException(HttpStatus.CONFLICT, type, title, detail, code);
    }

    private ApiProblemException validation(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/invoice-validation",
            "Invoice validation error",
            detail,
            "INVOICE_VALIDATION_ERROR"
        );
    }

    private InvoiceItemType parseItemType(String value) {
        InvoiceItemType type = InvoiceItemType.fromValue(value);
        if (type == null) {
            throw validation("itemType invalido. Valores: SERVICE, PRODUCT.");
        }
        return type;
    }

    private PaymentMethod parsePaymentMethod(String value) {
        PaymentMethod method = PaymentMethod.fromValue(value);
        if (method == null) {
            throw validation("method invalido. Valores: CASH, CARD, TRANSFER.");
        }
        return method;
    }

    private BigDecimal normalizeQty(BigDecimal value) {
        if (value == null) {
            throw validation("qty es requerido.");
        }
        BigDecimal normalized = value.setScale(3, RoundingMode.HALF_UP);
        if (normalized.compareTo(new BigDecimal("0.001")) < 0) {
            throw validation("qty debe ser >= 0.001.");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value, String detailIfNullOrNegative) {
        if (value == null) {
            throw validation(detailIfNullOrNegative);
        }
        BigDecimal normalized = roundCurrency(value);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw validation(detailIfNullOrNegative);
        }
        return normalized;
    }

    private BigDecimal normalizePositiveMoney(BigDecimal value, String detail) {
        if (value == null) {
            throw validation(detail);
        }
        BigDecimal normalized = roundCurrency(value);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw validation(detail);
        }
        return normalized;
    }

    private BigDecimal normalizeDiscount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal normalized = roundCurrency(value);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw validation("discountAmount no puede ser negativo.");
        }
        return normalized;
    }

    private String normalizeReason(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() < 10) {
            throw validation("reason es requerido (minimo 10 caracteres).");
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, int maxLength, String detail) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty() || normalized.length() > maxLength) {
            throw validation(detail);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw validation("Campo excede longitud maxima permitida.");
        }
        return normalized;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private BigDecimal roundCurrency(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal lineBase(BigDecimal qty, BigDecimal unitPrice) {
        return roundCurrency(qty.multiply(unitPrice));
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
                "https://sassveterinaria.local/errors/pdf-export-failed",
                "PDF export failed",
                "No se pudo generar el PDF.",
                "PDF_EXPORT_FAILED"
            );
        }
    }

    private Map<String, Object> snapshotInvoice(InvoiceEntity invoice) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", invoice.getId());
        data.put("status", invoice.getStatus());
        data.put("itemsSubtotal", invoice.getItemsSubtotal());
        data.put("discountAmount", invoice.getDiscountAmount());
        data.put("taxRate", invoice.getTaxRate());
        data.put("taxAmount", invoice.getTaxAmount());
        data.put("total", invoice.getTotal());
        data.put("voidReason", invoice.getVoidReason());
        data.put("voidedAt", invoice.getVoidedAt());
        data.put("updatedAt", invoice.getUpdatedAt());
        return data;
    }

    private Map<String, Object> snapshotItem(InvoiceItemEntity item) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", item.getId());
        data.put("invoiceId", item.getInvoiceId());
        data.put("itemType", item.getItemType());
        data.put("description", item.getDescription());
        data.put("qty", item.getQty());
        data.put("unitPrice", item.getUnitPrice());
        data.put("discountAmount", item.getDiscountAmount());
        data.put("lineTotal", item.getLineTotal());
        return data;
    }

    private InvoiceResponse toInvoiceResponse(InvoiceEntity entity) {
        BigDecimal paidTotal = invoicePaymentRepository.sumAmountByInvoice(entity.getBranchId(), entity.getId());
        return toInvoiceResponse(entity, paidTotal);
    }

    private InvoiceResponse toInvoiceResponse(InvoiceEntity entity, BigDecimal paidTotal) {
        return new InvoiceResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getVisitId(),
            entity.getInvoiceNumber(),
            entity.getStatus(),
            entity.getItemsSubtotal(),
            entity.getDiscountAmount(),
            entity.getTaxRate(),
            entity.getTaxAmount(),
            entity.getTotal(),
            paidTotal,
            entity.getVoidReason(),
            entity.getVoidedAt(),
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private InvoiceItemResponse toItemResponse(InvoiceItemEntity entity) {
        return new InvoiceItemResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getInvoiceId(),
            entity.getItemType(),
            entity.getItemId(),
            entity.getDescription(),
            entity.getQty(),
            entity.getUnitPrice(),
            entity.getDiscountAmount(),
            entity.getLineTotal(),
            entity.getCreatedAt()
        );
    }

    private InvoicePaymentResponse toPaymentResponse(InvoicePaymentEntity entity) {
        return new InvoicePaymentResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getInvoiceId(),
            entity.getMethod(),
            entity.getAmount(),
            entity.getReference(),
            entity.getCreatedBy(),
            entity.getCreatedAt()
        );
    }

    @FunctionalInterface
    private interface PdfWriterCallback {
        void write(Document document) throws DocumentException;
    }
}

package com.sassveterinaria.inventory.service;

import com.sassveterinaria.appointment.domain.ServiceEntity;
import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.audit.service.AuditService;
import com.sassveterinaria.clinical.domain.VisitEntity;
import com.sassveterinaria.clinical.service.VisitService;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.inventory.domain.ProductEntity;
import com.sassveterinaria.inventory.domain.ProductStockEntity;
import com.sassveterinaria.inventory.domain.ServiceBomItemEntity;
import com.sassveterinaria.inventory.domain.StockMovementEntity;
import com.sassveterinaria.inventory.domain.StockMovementType;
import com.sassveterinaria.inventory.domain.UnitEntity;
import com.sassveterinaria.inventory.dto.ConsumeMode;
import com.sassveterinaria.inventory.dto.InventoryConsumeItemRequest;
import com.sassveterinaria.inventory.dto.InventoryConsumeRequest;
import com.sassveterinaria.inventory.dto.InventoryConsumeResponse;
import com.sassveterinaria.inventory.dto.LowStockResponse;
import com.sassveterinaria.inventory.dto.ProductCreateRequest;
import com.sassveterinaria.inventory.dto.ProductPatchRequest;
import com.sassveterinaria.inventory.dto.ProductResponse;
import com.sassveterinaria.inventory.dto.ProductStockResponse;
import com.sassveterinaria.inventory.dto.ServiceBomItemRequest;
import com.sassveterinaria.inventory.dto.ServiceBomItemResponse;
import com.sassveterinaria.inventory.dto.ServiceBomReplaceRequest;
import com.sassveterinaria.inventory.dto.StockMovementCreateRequest;
import com.sassveterinaria.inventory.dto.StockMovementResponse;
import com.sassveterinaria.inventory.dto.UnitResponse;
import com.sassveterinaria.inventory.repo.ProductRepository;
import com.sassveterinaria.inventory.repo.ProductStockRepository;
import com.sassveterinaria.inventory.repo.ServiceBomItemRepository;
import com.sassveterinaria.inventory.repo.StockMovementRepository;
import com.sassveterinaria.inventory.repo.UnitRepository;
import com.sassveterinaria.security.AuthPrincipal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final String PERMISSION_STOCK_ADJUST = "STOCK_ADJUST";
    private static final String PERMISSION_STOCK_MOVE_CREATE = "STOCK_MOVE_CREATE";
    private static final String PERMISSION_STOCK_OVERRIDE_INVOICE = "STOCK_OVERRIDE_INVOICE";

    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ServiceBomItemRepository serviceBomItemRepository;
    private final ServiceRepository serviceRepository;
    private final VisitService visitService;
    private final AuditService auditService;

    public InventoryService(
        UnitRepository unitRepository,
        ProductRepository productRepository,
        ProductStockRepository productStockRepository,
        StockMovementRepository stockMovementRepository,
        ServiceBomItemRepository serviceBomItemRepository,
        ServiceRepository serviceRepository,
        VisitService visitService,
        AuditService auditService
    ) {
        this.unitRepository = unitRepository;
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.serviceBomItemRepository = serviceBomItemRepository;
        this.serviceRepository = serviceRepository;
        this.visitService = visitService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> listUnits() {
        return unitRepository.findByIsActiveTrueOrderByNameAsc().stream()
            .map(this::toUnitResponse)
            .toList();
    }

    @Transactional
    public ProductResponse createProduct(AuthPrincipal principal, ProductCreateRequest request) {
        String sku = normalizeOptionalSku(request.sku());
        ensureSkuAvailable(principal.getBranchId(), sku, null);

        UnitEntity unit = requireActiveUnit(request.unitId());
        ProductEntity product = new ProductEntity();
        product.setId(UUID.randomUUID());
        product.setBranchId(principal.getBranchId());
        product.setSku(sku);
        product.setName(normalizeName(request.name()));
        product.setUnitId(unit.getId());
        product.setMinQty(normalizeMinQty(request.minQty()));
        product.setActive(true);
        product.setCreatedAt(OffsetDateTime.now());
        ProductEntity saved = productRepository.save(product);

        ProductStockEntity stock = new ProductStockEntity();
        stock.setId(UUID.randomUUID());
        stock.setBranchId(saved.getBranchId());
        stock.setProductId(saved.getId());
        stock.setOnHandQty(zeroQty());
        stock.setAvgUnitCost(zeroCost());
        stock.setUpdatedAt(OffsetDateTime.now());
        productStockRepository.save(stock);

        return toProductResponse(saved, unit, stock);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(AuthPrincipal principal, String q, Boolean active, Boolean lowStock) {
        List<ProductEntity> products = productRepository.search(
            principal.getBranchId(),
            normalizeSearch(q),
            active,
            lowStock
        );
        return mapProductResponses(principal.getBranchId(), products);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(AuthPrincipal principal, UUID productId) {
        ProductEntity product = requireProduct(principal.getBranchId(), productId);
        return mapProductResponses(principal.getBranchId(), List.of(product)).getFirst();
    }

    @Transactional
    public ProductResponse patchProduct(AuthPrincipal principal, UUID productId, ProductPatchRequest request) {
        ProductEntity product = requireProduct(principal.getBranchId(), productId);

        if (request.sku() != null) {
            String sku = normalizeOptionalSku(request.sku());
            ensureSkuAvailable(principal.getBranchId(), sku, product.getId());
            product.setSku(sku);
        }
        if (request.name() != null) {
            product.setName(normalizeName(request.name()));
        }
        if (request.unitId() != null) {
            UnitEntity unit = requireActiveUnit(request.unitId());
            product.setUnitId(unit.getId());
        }
        if (request.minQty() != null) {
            product.setMinQty(normalizeMinQty(request.minQty()));
        }
        if (request.isActive() != null) {
            product.setActive(request.isActive());
        }

        ProductEntity saved = productRepository.save(product);
        return getProduct(principal, saved.getId());
    }

    @Transactional(readOnly = true)
    public ProductStockResponse getProductStock(AuthPrincipal principal, UUID productId) {
        ProductEntity product = requireProduct(principal.getBranchId(), productId);
        ProductStockEntity stock = productStockRepository.findByBranchIdAndProductId(principal.getBranchId(), productId)
            .orElseGet(() -> newStock(product.getBranchId(), productId));
        return toProductStockResponse(product, stock);
    }

    @Transactional(readOnly = true)
    public List<LowStockResponse> listLowStock(AuthPrincipal principal) {
        List<ProductEntity> lowProducts = productRepository.search(principal.getBranchId(), null, Boolean.TRUE, Boolean.TRUE);
        if (lowProducts.isEmpty()) {
            return List.of();
        }
        Map<UUID, ProductStockEntity> stockByProduct = stockMap(principal.getBranchId(), productIds(lowProducts));
        Map<UUID, UnitEntity> unitById = unitMap(unitIds(lowProducts));

        return lowProducts.stream()
            .map(product -> {
                ProductStockEntity stock = stockByProduct.get(product.getId());
                UnitEntity unit = unitById.get(product.getUnitId());
                BigDecimal onHand = stock == null ? zeroQty() : stock.getOnHandQty();
                return new LowStockResponse(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    unit == null ? null : unit.getCode(),
                    onHand,
                    product.getMinQty()
                );
            })
            .toList();
    }

    @Transactional
    public StockMovementResponse createMovement(AuthPrincipal principal, StockMovementCreateRequest request) {
        StockMovementType type = parseMovementType(request.type());
        if (type == StockMovementType.CONSUME) {
            throw validation("type CONSUME no es valido para este endpoint.");
        }
        if (type == StockMovementType.ADJUST) {
            requirePermission(principal, PERMISSION_STOCK_ADJUST);
        } else {
            requirePermission(principal, PERMISSION_STOCK_MOVE_CREATE);
        }

        ProductEntity product = requireProduct(principal.getBranchId(), request.productId());
        return applyMovement(
            principal,
            product,
            type,
            request.qty(),
            request.qtyDelta(),
            request.unitCost(),
            Boolean.TRUE.equals(request.override()),
            request.reason(),
            null
        );
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> listMovements(
        AuthPrincipal principal,
        UUID productId,
        OffsetDateTime from,
        OffsetDateTime to,
        String type
    ) {
        String normalizedType = null;
        if (type != null && !type.isBlank()) {
            normalizedType = parseMovementType(type).name();
        }
        List<StockMovementEntity> movements = stockMovementRepository.search(
            principal.getBranchId(),
            productId,
            from,
            to,
            normalizedType
        );
        return movements.stream().map(this::toMovementResponse).toList();
    }

    @Transactional
    public InventoryConsumeResponse consumeByVisit(AuthPrincipal principal, UUID visitId, InventoryConsumeRequest request) {
        requirePermission(principal, PERMISSION_STOCK_MOVE_CREATE);
        VisitEntity visit = visitService.requireVisit(principal, visitId);
        ConsumeMode mode = parseConsumeMode(request.mode());

        Map<UUID, BigDecimal> itemsToConsume = new LinkedHashMap<>();
        if (mode == ConsumeMode.BOM_ONLY || mode == ConsumeMode.BOM_PLUS_EXPLICIT) {
            List<ServiceBomItemEntity> bomItems = serviceBomItemRepository
                .findByBranchIdAndServiceIdOrderByProductIdAsc(principal.getBranchId(), visit.getServiceId());
            if (mode == ConsumeMode.BOM_ONLY && bomItems.isEmpty()) {
                throw validation("No existe BOM para el servicio de la visita.");
            }
            for (ServiceBomItemEntity item : bomItems) {
                itemsToConsume.merge(item.getProductId(), normalizePositiveQty(item.getQty(), "qty BOM invalida."), BigDecimal::add);
            }
        }

        if (mode == ConsumeMode.EXPLICIT_ONLY || mode == ConsumeMode.BOM_PLUS_EXPLICIT) {
            List<InventoryConsumeItemRequest> explicitItems = request.items();
            if ((explicitItems == null || explicitItems.isEmpty()) && mode == ConsumeMode.EXPLICIT_ONLY) {
                throw validation("items es requerido para EXPLICIT_ONLY.");
            }
            if (explicitItems != null) {
                for (InventoryConsumeItemRequest item : explicitItems) {
                    if (item.productId() == null) {
                        throw validation("productId es requerido en items.");
                    }
                    itemsToConsume.merge(item.productId(), normalizePositiveQty(item.qty(), "qty invalida en items."), BigDecimal::add);
                }
            }
        }

        if (itemsToConsume.isEmpty()) {
            throw validation("No hay items para consumir.");
        }

        boolean override = Boolean.TRUE.equals(request.override());
        List<StockMovementResponse> movements = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> entry : itemsToConsume.entrySet()) {
            ProductEntity product = requireProduct(principal.getBranchId(), entry.getKey());
            movements.add(applyMovement(
                principal,
                product,
                StockMovementType.CONSUME,
                entry.getValue(),
                null,
                null,
                override,
                request.reason(),
                visit.getId()
            ));
        }

        return new InventoryConsumeResponse(visit.getId(), mode.name(), movements);
    }

    @Transactional(readOnly = true)
    public List<ServiceBomItemResponse> getServiceBom(AuthPrincipal principal, UUID serviceId) {
        requireService(principal.getBranchId(), serviceId);
        List<ServiceBomItemEntity> items = serviceBomItemRepository
            .findByBranchIdAndServiceIdOrderByProductIdAsc(principal.getBranchId(), serviceId);
        return toBomResponses(principal.getBranchId(), items);
    }

    @Transactional
    public List<ServiceBomItemResponse> replaceServiceBom(
        AuthPrincipal principal,
        UUID serviceId,
        ServiceBomReplaceRequest request
    ) {
        ServiceEntity service = requireService(principal.getBranchId(), serviceId);

        List<ServiceBomItemEntity> beforeItems = serviceBomItemRepository
            .findByBranchIdAndServiceIdOrderByProductIdAsc(principal.getBranchId(), service.getId());
        Map<String, Object> before = Map.of("items", toBomResponses(principal.getBranchId(), beforeItems));

        List<ServiceBomItemRequest> requestItems = request.items() == null ? List.of() : request.items();
        Map<UUID, BigDecimal> normalized = normalizeBomItems(requestItems);
        if (!normalized.isEmpty()) {
            Collection<UUID> productIds = normalized.keySet();
            List<ProductEntity> products = productRepository.findByBranchIdAndIdIn(principal.getBranchId(), productIds);
            if (products.size() != productIds.size()) {
                throw validation("BOM contiene productos no existentes en la sucursal.");
            }
        }

        serviceBomItemRepository.deleteByBranchIdAndServiceId(principal.getBranchId(), service.getId());

        List<ServiceBomItemEntity> toSave = normalized.entrySet().stream()
            .map(entry -> {
                ServiceBomItemEntity entity = new ServiceBomItemEntity();
                entity.setId(UUID.randomUUID());
                entity.setBranchId(principal.getBranchId());
                entity.setServiceId(service.getId());
                entity.setProductId(entry.getKey());
                entity.setQty(entry.getValue());
                return entity;
            })
            .toList();
        List<ServiceBomItemEntity> saved = serviceBomItemRepository.saveAll(toSave);
        List<ServiceBomItemResponse> afterResponses = toBomResponses(principal.getBranchId(), saved);

        Map<String, Object> after = Map.of("items", afterResponses);
        auditService.record(
            principal,
            "SERVICE_BOM_REPLACE",
            "service",
            service.getId(),
            null,
            before,
            after
        );

        return afterResponses;
    }

    @Transactional
    public void validateInvoiceProductStock(
        AuthPrincipal principal,
        UUID invoiceId,
        UUID productId,
        BigDecimal requiredQty,
        boolean override,
        String reason
    ) {
        requireProduct(principal.getBranchId(), productId);
        BigDecimal normalizedRequiredQty = normalizePositiveQty(requiredQty, "qty de factura invalida.");
        ProductStockEntity stock = lockStock(principal.getBranchId(), productId);

        if (normalizedRequiredQty.compareTo(stock.getOnHandQty()) <= 0) {
            return;
        }

        if (!override) {
            throw insufficientStock(productId, stock.getOnHandQty(), normalizedRequiredQty);
        }

        String normalizedReason = normalizeReason(reason);
        requirePermission(principal, PERMISSION_STOCK_OVERRIDE_INVOICE);
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("invoiceId", invoiceId);
        before.put("productId", productId);
        before.put("onHandQty", stock.getOnHandQty());
        before.put("requiredQty", normalizedRequiredQty);
        before.put("override", false);

        Map<String, Object> after = new LinkedHashMap<>(before);
        after.put("override", true);
        after.put("reason", normalizedReason);

        auditService.record(
            principal,
            "STOCK_OVERRIDE_INVOICE",
            "invoice",
            invoiceId,
            normalizedReason,
            before,
            after
        );
    }

    @Transactional(readOnly = true)
    public String requireProductNameForBilling(AuthPrincipal principal, UUID productId) {
        return requireProduct(principal.getBranchId(), productId).getName();
    }

    private StockMovementResponse applyMovement(
        AuthPrincipal principal,
        ProductEntity product,
        StockMovementType type,
        BigDecimal qty,
        BigDecimal qtyDelta,
        BigDecimal inputUnitCost,
        boolean override,
        String inputReason,
        UUID visitId
    ) {
        ProductStockEntity stock = lockStock(principal.getBranchId(), product.getId());
        BigDecimal oldQty = stock.getOnHandQty();
        BigDecimal oldAvg = stock.getAvgUnitCost();

        BigDecimal delta;
        BigDecimal movementQty;
        BigDecimal movementUnitCost;
        String reason = normalizeOptionalReason(inputReason);

        switch (type) {
            case IN -> {
                movementQty = normalizePositiveQty(qty, "qty es requerido para IN.");
                movementUnitCost = normalizeCost(inputUnitCost, "unitCost es requerido para IN.");
                delta = movementQty;
            }
            case OUT, CONSUME -> {
                movementQty = normalizePositiveQty(qty, "qty es requerido.");
                movementUnitCost = oldAvg;
                delta = movementQty.negate();
            }
            case ADJUST -> {
                if (qtyDelta == null || qtyDelta.compareTo(BigDecimal.ZERO) == 0) {
                    throw validation("qtyDelta es requerido para ADJUST y no puede ser 0.");
                }
                movementQty = normalizePositiveQty(qtyDelta.abs(), "qtyDelta invalido.");
                movementUnitCost = oldAvg;
                delta = qtyDelta.setScale(3, RoundingMode.HALF_UP);
                reason = normalizeReason(inputReason);
            }
            default -> throw validation("type invalido.");
        }

        BigDecimal newQty = oldQty.add(delta).setScale(3, RoundingMode.HALF_UP);
        boolean overrideUsed = newQty.compareTo(BigDecimal.ZERO) < 0;
        if (overrideUsed) {
            if (!override) {
                throw insufficientStock(product.getId(), oldQty, movementQty);
            }
            requirePermission(principal, PERMISSION_STOCK_OVERRIDE_INVOICE);
            reason = normalizeReason(inputReason);
            Map<String, Object> before = stockSnapshot(stock);
            Map<String, Object> after = stockSnapshot(stock);
            after.put("onHandQty", newQty);
            after.put("override", true);
            auditService.record(
                principal,
                "STOCK_OVERRIDE_INVOICE",
                "product_stock",
                stock.getId(),
                reason,
                before,
                after
            );
        }

        BigDecimal newAvg = oldAvg;
        if (type == StockMovementType.IN) {
            if (newQty.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal numerator = oldQty.multiply(oldAvg).add(movementQty.multiply(movementUnitCost));
                newAvg = numerator.divide(newQty, 4, RoundingMode.HALF_UP);
            } else {
                newAvg = zeroCost();
            }
        } else if (newQty.compareTo(BigDecimal.ZERO) == 0) {
            newAvg = zeroCost();
        }

        stock.setOnHandQty(newQty);
        stock.setAvgUnitCost(newAvg);
        stock.setUpdatedAt(OffsetDateTime.now());
        productStockRepository.save(stock);

        StockMovementEntity movement = new StockMovementEntity();
        movement.setId(UUID.randomUUID());
        movement.setBranchId(principal.getBranchId());
        movement.setProductId(product.getId());
        movement.setType(type.name());
        movement.setQty(movementQty);
        movement.setUnitCost(movementUnitCost);
        movement.setTotalCost(movementQty.multiply(movementUnitCost).setScale(4, RoundingMode.HALF_UP));
        movement.setReason(reason);
        movement.setVisitId(visitId);
        movement.setCreatedBy(principal.getUserId());
        movement.setCreatedAt(OffsetDateTime.now());
        StockMovementEntity saved = stockMovementRepository.save(movement);
        auditService.recordEvent(
            principal,
            "STOCK_MOVE_CREATE",
            "stock_movement",
            saved.getId(),
            Map.of(
                "type", saved.getType(),
                "productId", saved.getProductId(),
                "qty", saved.getQty(),
                "unitCost", saved.getUnitCost(),
                "reason", saved.getReason()
            )
        );

        if (type == StockMovementType.ADJUST) {
            Map<String, Object> before = new LinkedHashMap<>();
            before.put("productId", product.getId());
            before.put("onHandQty", oldQty);
            before.put("avgUnitCost", oldAvg);
            Map<String, Object> after = new LinkedHashMap<>();
            after.put("productId", product.getId());
            after.put("onHandQty", newQty);
            after.put("avgUnitCost", newAvg);
            after.put("qtyDelta", delta);
            auditService.recordSensitiveEvent(
                principal,
                "STOCK_ADJUST",
                "product_stock",
                stock.getId(),
                reason,
                before,
                after
            );
        }

        return toMovementResponse(saved, newQty, newAvg);
    }

    private List<ProductResponse> mapProductResponses(UUID branchId, List<ProductEntity> products) {
        if (products.isEmpty()) {
            return List.of();
        }

        Map<UUID, UnitEntity> unitById = unitMap(unitIds(products));
        Map<UUID, ProductStockEntity> stockByProduct = stockMap(branchId, productIds(products));

        return products.stream()
            .map(product -> {
                UnitEntity unit = unitById.get(product.getUnitId());
                ProductStockEntity stock = stockByProduct.get(product.getId());
                return toProductResponse(product, unit, stock);
            })
            .toList();
    }

    private Map<UUID, UnitEntity> unitMap(Collection<UUID> unitIds) {
        if (unitIds.isEmpty()) {
            return Map.of();
        }
        return unitRepository.findByIdIn(unitIds).stream()
            .collect(HashMap::new, (map, unit) -> map.put(unit.getId(), unit), HashMap::putAll);
    }

    private Map<UUID, ProductStockEntity> stockMap(UUID branchId, Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return productStockRepository.findByBranchIdAndProductIdIn(branchId, productIds).stream()
            .collect(HashMap::new, (map, stock) -> map.put(stock.getProductId(), stock), HashMap::putAll);
    }

    private List<ServiceBomItemResponse> toBomResponses(UUID branchId, List<ServiceBomItemEntity> items) {
        if (items.isEmpty()) {
            return List.of();
        }
        List<UUID> productIds = items.stream().map(ServiceBomItemEntity::getProductId).toList();
        Map<UUID, ProductEntity> productById = productRepository.findByBranchIdAndIdIn(branchId, productIds)
            .stream()
            .collect(HashMap::new, (map, product) -> map.put(product.getId(), product), HashMap::putAll);
        return items.stream()
            .map(item -> {
                ProductEntity product = productById.get(item.getProductId());
                return new ServiceBomItemResponse(
                    item.getId(),
                    item.getBranchId(),
                    item.getServiceId(),
                    item.getProductId(),
                    product == null ? null : product.getName(),
                    product == null ? null : product.getSku(),
                    item.getQty()
                );
            })
            .toList();
    }

    private ProductResponse toProductResponse(ProductEntity product, UnitEntity unit, ProductStockEntity stock) {
        ProductStockEntity effectiveStock = stock == null ? newStock(product.getBranchId(), product.getId()) : stock;
        BigDecimal onHand = effectiveStock.getOnHandQty();
        return new ProductResponse(
            product.getId(),
            product.getBranchId(),
            product.getSku(),
            product.getName(),
            product.getUnitId(),
            unit == null ? null : unit.getCode(),
            unit == null ? null : unit.getName(),
            product.getMinQty(),
            product.isActive(),
            onHand,
            effectiveStock.getAvgUnitCost(),
            onHand.compareTo(product.getMinQty()) <= 0,
            product.getCreatedAt(),
            effectiveStock.getUpdatedAt()
        );
    }

    private ProductStockResponse toProductStockResponse(ProductEntity product, ProductStockEntity stock) {
        ProductStockEntity effectiveStock = stock == null ? newStock(product.getBranchId(), product.getId()) : stock;
        BigDecimal onHand = effectiveStock.getOnHandQty();
        return new ProductStockResponse(
            product.getId(),
            product.getBranchId(),
            onHand,
            effectiveStock.getAvgUnitCost(),
            product.getMinQty(),
            onHand.compareTo(product.getMinQty()) <= 0,
            effectiveStock.getUpdatedAt()
        );
    }

    private UnitResponse toUnitResponse(UnitEntity entity) {
        return new UnitResponse(entity.getId(), entity.getCode(), entity.getName(), entity.isActive());
    }

    private StockMovementResponse toMovementResponse(StockMovementEntity movement) {
        return new StockMovementResponse(
            movement.getId(),
            movement.getBranchId(),
            movement.getProductId(),
            movement.getType(),
            movement.getQty(),
            movement.getUnitCost(),
            movement.getTotalCost(),
            movement.getReason(),
            movement.getVisitId(),
            movement.getCreatedBy(),
            movement.getCreatedAt(),
            null,
            null
        );
    }

    private StockMovementResponse toMovementResponse(StockMovementEntity movement, BigDecimal onHandAfter, BigDecimal avgAfter) {
        return new StockMovementResponse(
            movement.getId(),
            movement.getBranchId(),
            movement.getProductId(),
            movement.getType(),
            movement.getQty(),
            movement.getUnitCost(),
            movement.getTotalCost(),
            movement.getReason(),
            movement.getVisitId(),
            movement.getCreatedBy(),
            movement.getCreatedAt(),
            onHandAfter,
            avgAfter
        );
    }

    private ProductEntity requireProduct(UUID branchId, UUID productId) {
        if (productId == null) {
            throw validation("productId es requerido.");
        }
        return productRepository.findByIdAndBranchId(productId, branchId)
            .orElseThrow(() -> notFound(
                "https://sassveterinaria.local/errors/product-not-found",
                "Product not found",
                "No se encontro el producto.",
                "PRODUCT_NOT_FOUND"
            ));
    }

    private UnitEntity requireActiveUnit(UUID unitId) {
        if (unitId == null) {
            throw validation("unitId es requerido.");
        }
        return unitRepository.findByIdAndIsActiveTrue(unitId)
            .orElseThrow(() -> notFound(
                "https://sassveterinaria.local/errors/unit-not-found",
                "Unit not found",
                "No se encontro la unidad.",
                "UNIT_NOT_FOUND"
            ));
    }

    private ServiceEntity requireService(UUID branchId, UUID serviceId) {
        return serviceRepository.findByIdAndBranchId(serviceId, branchId)
            .orElseThrow(() -> notFound(
                "https://sassveterinaria.local/errors/service-not-found",
                "Service not found",
                "No se encontro el servicio.",
                "SERVICE_NOT_FOUND"
            ));
    }

    private ProductStockEntity lockStock(UUID branchId, UUID productId) {
        return productStockRepository.findByBranchIdAndProductIdForUpdate(branchId, productId)
            .orElseGet(() -> {
                productStockRepository.saveAndFlush(newStock(branchId, productId));
                return productStockRepository.findByBranchIdAndProductIdForUpdate(branchId, productId)
                    .orElseThrow(() -> new IllegalStateException("Cannot lock stock row"));
            });
    }

    private ProductStockEntity newStock(UUID branchId, UUID productId) {
        ProductStockEntity stock = new ProductStockEntity();
        stock.setId(UUID.randomUUID());
        stock.setBranchId(branchId);
        stock.setProductId(productId);
        stock.setOnHandQty(zeroQty());
        stock.setAvgUnitCost(zeroCost());
        stock.setUpdatedAt(OffsetDateTime.now());
        return stock;
    }

    private void ensureSkuAvailable(UUID branchId, String sku, UUID currentProductId) {
        if (sku == null) {
            return;
        }
        productRepository.findByBranchIdAndSkuIgnoreCase(branchId, sku).ifPresent(existing -> {
            if (currentProductId == null || !existing.getId().equals(currentProductId)) {
                throw conflict(
                    "https://sassveterinaria.local/errors/product-sku-conflict",
                    "Product SKU conflict",
                    "Ya existe un producto con ese sku en la sucursal.",
                    "PRODUCT_SKU_CONFLICT"
                );
            }
        });
    }

    private Map<UUID, BigDecimal> normalizeBomItems(List<ServiceBomItemRequest> items) {
        Map<UUID, BigDecimal> normalized = new LinkedHashMap<>();
        for (ServiceBomItemRequest item : items) {
            if (item.productId() == null) {
                throw validation("productId es requerido en BOM.");
            }
            if (normalized.containsKey(item.productId())) {
                throw validation("BOM contiene productos duplicados.");
            }
            normalized.put(item.productId(), normalizePositiveQty(item.qty(), "qty invalida en BOM."));
        }
        return normalized;
    }

    private List<UUID> productIds(List<ProductEntity> products) {
        return products.stream().map(ProductEntity::getId).toList();
    }

    private List<UUID> unitIds(List<ProductEntity> products) {
        return products.stream().map(ProductEntity::getUnitId).distinct().toList();
    }

    private String normalizeName(String value) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.length() < 3 || normalized.length() > 160) {
            throw validation("name debe tener entre 3 y 160 caracteres.");
        }
        return normalized;
    }

    private String normalizeOptionalSku(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 60) {
            throw validation("sku no puede exceder 60 caracteres.");
        }
        return normalized;
    }

    private String normalizeSearch(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private BigDecimal normalizeMinQty(BigDecimal value) {
        if (value == null) {
            return zeroQty();
        }
        BigDecimal normalized = value.setScale(3, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw validation("minQty debe ser >= 0.");
        }
        return normalized;
    }

    private BigDecimal normalizePositiveQty(BigDecimal value, String detail) {
        if (value == null) {
            throw validation(detail);
        }
        BigDecimal normalized = value.setScale(3, RoundingMode.HALF_UP);
        if (normalized.compareTo(new BigDecimal("0.001")) < 0) {
            throw validation(detail);
        }
        return normalized;
    }

    private BigDecimal normalizeCost(BigDecimal value, String detail) {
        if (value == null) {
            throw validation(detail);
        }
        BigDecimal normalized = value.setScale(4, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw validation(detail);
        }
        return normalized;
    }

    private String normalizeOptionalReason(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeReason(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() < 10) {
            throw validation("reason es requerido (minimo 10 caracteres).");
        }
        return normalized;
    }

    private StockMovementType parseMovementType(String value) {
        StockMovementType type = StockMovementType.fromValue(value);
        if (type == null) {
            throw validation("type invalido. Valores: IN, OUT, ADJUST, CONSUME.");
        }
        return type;
    }

    private ConsumeMode parseConsumeMode(String value) {
        ConsumeMode mode = ConsumeMode.fromValue(value);
        if (mode == null) {
            throw validation("mode invalido. Valores: BOM_ONLY, EXPLICIT_ONLY, BOM_PLUS_EXPLICIT.");
        }
        return mode;
    }

    private Map<String, Object> stockSnapshot(ProductStockEntity stock) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", stock.getId());
        snapshot.put("branchId", stock.getBranchId());
        snapshot.put("productId", stock.getProductId());
        snapshot.put("onHandQty", stock.getOnHandQty());
        snapshot.put("avgUnitCost", stock.getAvgUnitCost());
        snapshot.put("updatedAt", stock.getUpdatedAt());
        return snapshot;
    }

    private void requirePermission(AuthPrincipal principal, String permission) {
        if (principal.getPermissions() == null || !principal.getPermissions().contains(permission)) {
            throw new ApiProblemException(
                HttpStatus.FORBIDDEN,
                "https://sassveterinaria.local/errors/forbidden",
                "Forbidden",
                "No tienes permisos para esta accion.",
                "FORBIDDEN"
            );
        }
    }

    private ApiProblemException insufficientStock(UUID productId, BigDecimal onHandQty, BigDecimal requiredQty) {
        String detail = String.format(
            "insufficient_stock: productId=%s onHandQty=%s requiredQty=%s",
            productId,
            onHandQty,
            requiredQty
        );
        return new ApiProblemException(
            HttpStatus.CONFLICT,
            "https://sassveterinaria.local/errors/insufficient-stock",
            "Insufficient stock",
            detail,
            "INSUFFICIENT_STOCK"
        );
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
            "https://sassveterinaria.local/errors/inventory-validation",
            "Inventory validation error",
            detail,
            "INVENTORY_VALIDATION_ERROR"
        );
    }

    private BigDecimal zeroQty() {
        return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroCost() {
        return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }
}

package com.sassveterinaria.billing.service;

import com.sassveterinaria.audit.service.AuditService;
import com.sassveterinaria.billing.domain.TaxConfigEntity;
import com.sassveterinaria.billing.dto.TaxConfigResponse;
import com.sassveterinaria.billing.dto.TaxConfigUpdateRequest;
import com.sassveterinaria.billing.repo.TaxConfigRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.AuthPrincipal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxConfigService {

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.1500");

    private final TaxConfigRepository taxConfigRepository;
    private final AuditService auditService;

    public TaxConfigService(TaxConfigRepository taxConfigRepository, AuditService auditService) {
        this.taxConfigRepository = taxConfigRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public TaxConfigResponse get(AuthPrincipal principal) {
        return toResponse(requireOrCreate(principal));
    }

    @Transactional
    public TaxConfigResponse update(AuthPrincipal principal, TaxConfigUpdateRequest request) {
        TaxConfigEntity entity = requireOrCreate(principal);
        BigDecimal newRate = normalizeRate(request.taxRate());
        if (entity.getTaxRate().compareTo(newRate) != 0) {
            String reason = normalizeReason(request.reason());
            Map<String, Object> before = snapshot(entity);
            entity.setTaxRate(newRate);
            entity.setUpdatedBy(principal.getUserId());
            entity.setUpdatedAt(OffsetDateTime.now());
            TaxConfigEntity saved = taxConfigRepository.save(entity);
            auditService.record(
                principal,
                "CONFIG_TAX_UPDATE",
                "tax_config",
                saved.getId(),
                reason,
                before,
                snapshot(saved)
            );
            return toResponse(saved);
        }
        return toResponse(entity);
    }

    @Transactional
    public TaxConfigEntity requireOrCreate(AuthPrincipal principal) {
        return taxConfigRepository.findByBranchId(principal.getBranchId()).orElseGet(() -> {
            TaxConfigEntity entity = new TaxConfigEntity();
            entity.setId(UUID.randomUUID());
            entity.setBranchId(principal.getBranchId());
            entity.setTaxRate(DEFAULT_TAX_RATE);
            entity.setUpdatedBy(principal.getUserId());
            entity.setUpdatedAt(OffsetDateTime.now());
            return taxConfigRepository.save(entity);
        });
    }

    private BigDecimal normalizeRate(BigDecimal value) {
        if (value == null) {
            throw validation("taxRate es requerido.");
        }
        BigDecimal normalized = value.setScale(4, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) < 0 || normalized.compareTo(BigDecimal.ONE) > 0) {
            throw validation("taxRate debe estar entre 0.0000 y 1.0000.");
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

    private Map<String, Object> snapshot(TaxConfigEntity entity) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", entity.getId());
        data.put("branchId", entity.getBranchId());
        data.put("taxRate", entity.getTaxRate());
        data.put("updatedBy", entity.getUpdatedBy());
        data.put("updatedAt", entity.getUpdatedAt());
        return data;
    }

    private ApiProblemException validation(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/tax-config-validation",
            "Tax config validation error",
            detail,
            "TAX_CONFIG_VALIDATION_ERROR"
        );
    }

    private TaxConfigResponse toResponse(TaxConfigEntity entity) {
        return new TaxConfigResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getTaxRate(),
            entity.getUpdatedBy(),
            entity.getUpdatedAt()
        );
    }
}

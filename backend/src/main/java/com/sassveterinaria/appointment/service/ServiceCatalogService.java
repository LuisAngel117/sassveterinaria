package com.sassveterinaria.appointment.service;

import com.sassveterinaria.appointment.domain.ServiceEntity;
import com.sassveterinaria.appointment.dto.ServiceCreateRequest;
import com.sassveterinaria.appointment.dto.ServicePatchRequest;
import com.sassveterinaria.appointment.dto.ServiceResponse;
import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.audit.service.AuditService;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.AuthPrincipal;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final AuditService auditService;

    public ServiceCatalogService(ServiceRepository serviceRepository, AuditService auditService) {
        this.serviceRepository = serviceRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ServiceResponse create(AuthPrincipal principal, ServiceCreateRequest request) {
        String name = normalizeName(request.name());
        ensureNameAvailable(principal.getBranchId(), name, null);

        ServiceEntity entity = new ServiceEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setName(name);
        entity.setDurationMinutes(validateDuration(request.durationMinutes()));
        entity.setPriceBase(validatePrice(request.priceBase()));
        entity.setActive(true);
        entity.setCreatedAt(OffsetDateTime.now());

        return toResponse(serviceRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> list(AuthPrincipal principal, Boolean active, String q) {
        Boolean activeFilter = active == null ? Boolean.TRUE : active;
        return serviceRepository
            .search(principal.getBranchId(), activeFilter, normalizeSearch(q))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse get(AuthPrincipal principal, UUID serviceId) {
        return toResponse(requireService(principal, serviceId));
    }

    @Transactional
    public ServiceResponse update(AuthPrincipal principal, UUID serviceId, ServicePatchRequest request) {
        ServiceEntity entity = requireService(principal, serviceId);
        Map<String, Object> beforePayload = snapshot(entity);

        if (request.name() != null) {
            String name = normalizeName(request.name());
            ensureNameAvailable(principal.getBranchId(), name, entity.getId());
            entity.setName(name);
        }

        if (request.durationMinutes() != null) {
            entity.setDurationMinutes(validateDuration(request.durationMinutes()));
        }

        boolean priceChanged = false;
        if (request.priceBase() != null) {
            BigDecimal newPrice = validatePrice(request.priceBase());
            priceChanged = entity.getPriceBase().compareTo(newPrice) != 0;
            entity.setPriceBase(newPrice);
        }

        if (request.isActive() != null) {
            entity.setActive(request.isActive());
        }

        if (priceChanged) {
            String reason = normalizeReason(request.reason());
            auditService.record(
                principal,
                "SERVICE_PRICE_UPDATE",
                "service",
                entity.getId(),
                reason,
                beforePayload,
                snapshot(entity)
            );
        }

        return toResponse(serviceRepository.save(entity));
    }

    private ServiceEntity requireService(AuthPrincipal principal, UUID serviceId) {
        return serviceRepository.findByIdAndBranchId(serviceId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/service-not-found",
                "Service not found",
                "No se encontro el servicio.",
                "SERVICE_NOT_FOUND"
            ));
    }

    private void ensureNameAvailable(UUID branchId, String name, UUID currentServiceId) {
        boolean exists = currentServiceId == null
            ? serviceRepository.existsByBranchIdAndNameIgnoreCase(branchId, name)
            : serviceRepository.existsByBranchIdAndNameIgnoreCaseAndIdNot(branchId, name, currentServiceId);

        if (exists) {
            throw new ApiProblemException(
                HttpStatus.CONFLICT,
                "https://sassveterinaria.local/errors/service-name-conflict",
                "Service name conflict",
                "Ya existe un servicio con ese nombre en la sucursal.",
                "SERVICE_NAME_CONFLICT"
            );
        }
    }

    private String normalizeName(String name) {
        String normalized = name == null ? null : name.trim();
        if (normalized == null || normalized.length() < 3 || normalized.length() > 120) {
            throw validationError("name debe tener entre 3 y 120 caracteres.");
        }
        return normalized;
    }

    private int validateDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes < 5 || durationMinutes > 480) {
            throw validationError("durationMinutes debe estar entre 5 y 480.");
        }
        return durationMinutes;
    }

    private BigDecimal validatePrice(BigDecimal priceBase) {
        if (priceBase == null || priceBase.compareTo(BigDecimal.ZERO) < 0 || priceBase.compareTo(new BigDecimal("99999.99")) > 0) {
            throw validationError("priceBase debe estar entre 0.00 y 99999.99.");
        }
        return priceBase;
    }

    private String normalizeSearch(String q) {
        if (q == null) {
            return null;
        }
        String normalized = q.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeReason(String reason) {
        String normalized = reason == null ? "" : reason.trim();
        if (normalized.length() < 10) {
            throw validationError("reason es requerido (minimo 10 caracteres) cuando cambia priceBase.");
        }
        return normalized;
    }

    private ApiProblemException validationError(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/service-validation",
            "Service validation error",
            detail,
            "SERVICE_VALIDATION_ERROR"
        );
    }

    private Map<String, Object> snapshot(ServiceEntity entity) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", entity.getId());
        data.put("branchId", entity.getBranchId());
        data.put("name", entity.getName());
        data.put("durationMinutes", entity.getDurationMinutes());
        data.put("priceBase", entity.getPriceBase());
        data.put("isActive", entity.isActive());
        return data;
    }

    private ServiceResponse toResponse(ServiceEntity entity) {
        return new ServiceResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getName(),
            entity.getDurationMinutes(),
            entity.getPriceBase(),
            entity.isActive(),
            entity.getCreatedAt()
        );
    }
}

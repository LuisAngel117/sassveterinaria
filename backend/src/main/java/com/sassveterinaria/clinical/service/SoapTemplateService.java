package com.sassveterinaria.clinical.service;

import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.clinical.domain.SoapTemplateEntity;
import com.sassveterinaria.clinical.dto.SoapTemplateCreateRequest;
import com.sassveterinaria.clinical.dto.SoapTemplatePatchRequest;
import com.sassveterinaria.clinical.dto.SoapTemplateResponse;
import com.sassveterinaria.clinical.repo.SoapTemplateRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoapTemplateService {

    private final SoapTemplateRepository soapTemplateRepository;
    private final ServiceRepository serviceRepository;

    public SoapTemplateService(SoapTemplateRepository soapTemplateRepository, ServiceRepository serviceRepository) {
        this.soapTemplateRepository = soapTemplateRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional
    public SoapTemplateResponse create(AuthPrincipal principal, SoapTemplateCreateRequest request) {
        requireService(principal, request.serviceId());

        SoapTemplateEntity entity = new SoapTemplateEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setServiceId(request.serviceId());
        entity.setName(normalizeRequired(request.name(), "name es requerido."));
        entity.setSReason(normalizeText(request.sReason()));
        entity.setSAnamnesis(normalizeText(request.sAnamnesis()));
        entity.setOFindings(normalizeText(request.oFindings()));
        entity.setADiagnosis(normalizeText(request.aDiagnosis()));
        entity.setPTreatment(normalizeText(request.pTreatment()));
        entity.setPInstructions(normalizeText(request.pInstructions()));
        entity.setActive(true);
        entity.setCreatedAt(OffsetDateTime.now());
        return toResponse(soapTemplateRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<SoapTemplateResponse> listByService(AuthPrincipal principal, UUID serviceId) {
        requireService(principal, serviceId);
        return soapTemplateRepository
            .findByBranchIdAndServiceIdOrderByNameAsc(principal.getBranchId(), serviceId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SoapTemplateResponse get(AuthPrincipal principal, UUID templateId) {
        return toResponse(requireTemplate(principal, templateId));
    }

    @Transactional
    public SoapTemplateResponse update(AuthPrincipal principal, UUID templateId, SoapTemplatePatchRequest request) {
        SoapTemplateEntity entity = requireTemplate(principal, templateId);

        if (request.name() != null) {
            entity.setName(normalizeRequired(request.name(), "name no puede ser vacio."));
        }
        if (request.sReason() != null) {
            entity.setSReason(normalizeText(request.sReason()));
        }
        if (request.sAnamnesis() != null) {
            entity.setSAnamnesis(normalizeText(request.sAnamnesis()));
        }
        if (request.oFindings() != null) {
            entity.setOFindings(normalizeText(request.oFindings()));
        }
        if (request.aDiagnosis() != null) {
            entity.setADiagnosis(normalizeText(request.aDiagnosis()));
        }
        if (request.pTreatment() != null) {
            entity.setPTreatment(normalizeText(request.pTreatment()));
        }
        if (request.pInstructions() != null) {
            entity.setPInstructions(normalizeText(request.pInstructions()));
        }
        if (request.isActive() != null) {
            entity.setActive(request.isActive());
        }

        return toResponse(soapTemplateRepository.save(entity));
    }

    private void requireService(AuthPrincipal principal, UUID serviceId) {
        serviceRepository.findByIdAndBranchId(serviceId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/service-not-found",
                "Service not found",
                "No se encontro el servicio.",
                "SERVICE_NOT_FOUND"
            ));
    }

    private SoapTemplateEntity requireTemplate(AuthPrincipal principal, UUID templateId) {
        return soapTemplateRepository.findByIdAndBranchId(templateId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/template-not-found",
                "Template not found",
                "No se encontro la plantilla SOAP.",
                "TEMPLATE_NOT_FOUND"
            ));
    }

    private String normalizeRequired(String value, String detail) {
        String normalized = normalizeText(value);
        if (normalized == null || normalized.length() < 3 || normalized.length() > 120) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/template-validation",
                "Template validation error",
                detail,
                "TEMPLATE_VALIDATION_ERROR"
            );
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

    private SoapTemplateResponse toResponse(SoapTemplateEntity entity) {
        return new SoapTemplateResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getServiceId(),
            entity.getName(),
            entity.getSReason(),
            entity.getSAnamnesis(),
            entity.getOFindings(),
            entity.getADiagnosis(),
            entity.getPTreatment(),
            entity.getPInstructions(),
            entity.isActive(),
            entity.getCreatedAt()
        );
    }
}

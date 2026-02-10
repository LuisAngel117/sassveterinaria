package com.sassveterinaria.clinical.service;

import com.sassveterinaria.appointment.repo.AppointmentRepository;
import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.audit.service.AuditService;
import com.sassveterinaria.clinical.domain.SoapTemplateEntity;
import com.sassveterinaria.clinical.domain.VisitEntity;
import com.sassveterinaria.clinical.domain.VisitStatus;
import com.sassveterinaria.clinical.dto.VisitCreateRequest;
import com.sassveterinaria.clinical.dto.VisitPatchRequest;
import com.sassveterinaria.clinical.dto.VisitResponse;
import com.sassveterinaria.clinical.repo.SoapTemplateRepository;
import com.sassveterinaria.clinical.repo.VisitRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.crm.repo.PetRepository;
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
public class VisitService {

    private final VisitRepository visitRepository;
    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final SoapTemplateRepository soapTemplateRepository;
    private final AuditService auditService;

    public VisitService(
        VisitRepository visitRepository,
        PetRepository petRepository,
        ServiceRepository serviceRepository,
        AppointmentRepository appointmentRepository,
        SoapTemplateRepository soapTemplateRepository,
        AuditService auditService
    ) {
        this.visitRepository = visitRepository;
        this.petRepository = petRepository;
        this.serviceRepository = serviceRepository;
        this.appointmentRepository = appointmentRepository;
        this.soapTemplateRepository = soapTemplateRepository;
        this.auditService = auditService;
    }

    @Transactional
    public VisitResponse create(AuthPrincipal principal, VisitCreateRequest request) {
        requirePet(principal, request.petId());
        requireService(principal, request.serviceId());

        if (request.appointmentId() != null) {
            requireAppointment(principal, request.appointmentId());
        }

        SoapTemplateEntity template = null;
        if (request.templateId() != null) {
            template = requireTemplate(principal, request.templateId());
            if (!template.getServiceId().equals(request.serviceId())) {
                throw validation("templateId no corresponde al serviceId de la visita.");
            }
        }

        String sReason = firstText(request.sReason(), template == null ? null : template.getSReason());
        String sAnamnesis = firstText(request.sAnamnesis(), template == null ? null : template.getSAnamnesis());
        if (isBlank(sReason) || isBlank(sAnamnesis)) {
            throw validation("sReason y sAnamnesis son requeridos para crear la visita.");
        }

        VisitEntity entity = new VisitEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setPetId(request.petId());
        entity.setServiceId(request.serviceId());
        entity.setAppointmentId(request.appointmentId());
        entity.setStatus(VisitStatus.OPEN.name());
        entity.setSReason(sReason);
        entity.setSAnamnesis(sAnamnesis);
        entity.setOWeightKg(normalizeWeight(request.oWeightKg()));
        entity.setOTemperatureC(normalizeTemperature(request.oTemperatureC()));
        entity.setOFindings(firstText(request.oFindings(), template == null ? null : template.getOFindings()));
        entity.setADiagnosis(firstText(request.aDiagnosis(), template == null ? null : template.getADiagnosis()));
        entity.setASeverity(normalizeOptional(request.aSeverity(), 30));
        entity.setPTreatment(firstText(request.pTreatment(), template == null ? null : template.getPTreatment()));
        entity.setPInstructions(firstText(request.pInstructions(), template == null ? null : template.getPInstructions()));
        entity.setPFollowupAt(request.pFollowupAt());
        entity.setCreatedBy(principal.getUserId());
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return toResponse(visitRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public VisitResponse get(AuthPrincipal principal, UUID visitId) {
        return toResponse(requireVisit(principal, visitId));
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> listByPet(
        AuthPrincipal principal,
        UUID petId,
        String status,
        OffsetDateTime from,
        OffsetDateTime to
    ) {
        requirePet(principal, petId);

        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            VisitStatus parsed = VisitStatus.fromValue(status);
            if (parsed == null) {
                throw validation("status invalido.");
            }
            normalizedStatus = parsed.name();
        }

        return visitRepository
            .searchByPet(principal.getBranchId(), petId, normalizedStatus, from, to)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public VisitResponse update(AuthPrincipal principal, UUID visitId, VisitPatchRequest request) {
        VisitEntity entity = requireVisit(principal, visitId);
        requireVisitOpen(entity);

        if (request.sReason() != null) {
            String value = normalizeRequired(request.sReason(), "sReason no puede ser vacio.");
            entity.setSReason(value);
        }
        if (request.sAnamnesis() != null) {
            String value = normalizeRequired(request.sAnamnesis(), "sAnamnesis no puede ser vacio.");
            entity.setSAnamnesis(value);
        }
        if (request.oWeightKg() != null) {
            entity.setOWeightKg(normalizeWeight(request.oWeightKg()));
        }
        if (request.oTemperatureC() != null) {
            entity.setOTemperatureC(normalizeTemperature(request.oTemperatureC()));
        }
        if (request.oFindings() != null) {
            entity.setOFindings(normalizeText(request.oFindings()));
        }
        if (request.aDiagnosis() != null) {
            entity.setADiagnosis(normalizeText(request.aDiagnosis()));
        }
        if (request.aSeverity() != null) {
            entity.setASeverity(normalizeOptional(request.aSeverity(), 30));
        }
        if (request.pTreatment() != null) {
            entity.setPTreatment(normalizeText(request.pTreatment()));
        }
        if (request.pInstructions() != null) {
            entity.setPInstructions(normalizeText(request.pInstructions()));
        }
        if (request.pFollowupAt() != null) {
            entity.setPFollowupAt(request.pFollowupAt());
        }
        entity.setUpdatedAt(OffsetDateTime.now());

        return toResponse(visitRepository.save(entity));
    }

    @Transactional
    public VisitResponse close(AuthPrincipal principal, UUID visitId) {
        VisitEntity entity = requireVisit(principal, visitId);
        VisitStatus status = VisitStatus.fromValue(entity.getStatus());
        if (status == VisitStatus.CLOSED) {
            throw visitClosed();
        }
        entity.setStatus(VisitStatus.CLOSED.name());
        entity.setUpdatedAt(OffsetDateTime.now());
        return toResponse(visitRepository.save(entity));
    }

    @Transactional
    public VisitResponse reopen(AuthPrincipal principal, UUID visitId, String reason) {
        VisitEntity entity = requireVisit(principal, visitId);
        VisitStatus status = VisitStatus.fromValue(entity.getStatus());
        if (status != VisitStatus.CLOSED) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/visit-invalid-transition",
                "Visit invalid transition",
                "Solo se puede reabrir una visita en estado CLOSED.",
                "VISIT_INVALID_TRANSITION"
            );
        }

        String normalizedReason = normalizeReason(reason);
        Map<String, Object> before = snapshot(entity);
        entity.setStatus(VisitStatus.OPEN.name());
        entity.setUpdatedAt(OffsetDateTime.now());
        VisitEntity saved = visitRepository.save(entity);
        auditService.record(
            principal,
            "VISIT_REOPEN",
            "visit",
            saved.getId(),
            normalizedReason,
            before,
            snapshot(saved)
        );
        return toResponse(saved);
    }

    public VisitEntity requireVisit(AuthPrincipal principal, UUID visitId) {
        return visitRepository.findByIdAndBranchId(visitId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/visit-not-found",
                "Visit not found",
                "No se encontro la visita.",
                "VISIT_NOT_FOUND"
            ));
    }

    public void requireVisitOpen(VisitEntity entity) {
        VisitStatus status = VisitStatus.fromValue(entity.getStatus());
        if (status == VisitStatus.CLOSED) {
            throw visitClosed();
        }
    }

    private void requirePet(AuthPrincipal principal, UUID petId) {
        petRepository.findByIdAndBranchId(petId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/pet-not-found",
                "Pet not found",
                "No se encontro la mascota.",
                "PET_NOT_FOUND"
            ));
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

    private void requireAppointment(AuthPrincipal principal, UUID appointmentId) {
        appointmentRepository.findByIdAndBranchId(appointmentId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/appointment-not-found",
                "Appointment not found",
                "No se encontro la cita.",
                "APPT_NOT_FOUND"
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

    private ApiProblemException visitClosed() {
        return new ApiProblemException(
            HttpStatus.CONFLICT,
            "https://sassveterinaria.local/errors/visit-closed",
            "Visit closed",
            "La visita esta cerrada y no permite modificaciones.",
            "VISIT_CLOSED"
        );
    }

    private ApiProblemException validation(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/visit-validation",
            "Visit validation error",
            detail,
            "VISIT_VALIDATION_ERROR"
        );
    }

    private String normalizeReason(String reason) {
        String normalized = reason == null ? "" : reason.trim();
        if (normalized.length() < 10) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/reason-required",
                "Reason required",
                "reason es requerido (minimo 10 caracteres).",
                "REASON_REQUIRED"
            );
        }
        return normalized;
    }

    private String normalizeRequired(String value, String detail) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw validation(detail);
        }
        return normalized;
    }

    private String normalizeOptional(String value, int maxLength) {
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

    private String firstText(String first, String second) {
        String normalizedFirst = normalizeText(first);
        if (normalizedFirst != null) {
            return normalizedFirst;
        }
        return normalizeText(second);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BigDecimal normalizeWeight(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(new BigDecimal("9999.99")) > 0) {
            throw validation("oWeightKg debe ser mayor a 0 y menor o igual a 9999.99.");
        }
        return value;
    }

    private BigDecimal normalizeTemperature(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(new BigDecimal("20.0")) < 0 || value.compareTo(new BigDecimal("50.0")) > 0) {
            throw validation("oTemperatureC debe estar entre 20.0 y 50.0.");
        }
        return value;
    }

    private Map<String, Object> snapshot(VisitEntity entity) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", entity.getId());
        data.put("status", entity.getStatus());
        data.put("sReason", entity.getSReason());
        data.put("sAnamnesis", entity.getSAnamnesis());
        data.put("oFindings", entity.getOFindings());
        data.put("aDiagnosis", entity.getADiagnosis());
        data.put("pTreatment", entity.getPTreatment());
        data.put("pInstructions", entity.getPInstructions());
        data.put("updatedAt", entity.getUpdatedAt());
        return data;
    }

    private VisitResponse toResponse(VisitEntity entity) {
        return new VisitResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getPetId(),
            entity.getServiceId(),
            entity.getAppointmentId(),
            entity.getStatus(),
            entity.getSReason(),
            entity.getSAnamnesis(),
            entity.getOWeightKg(),
            entity.getOTemperatureC(),
            entity.getOFindings(),
            entity.getADiagnosis(),
            entity.getASeverity(),
            entity.getPTreatment(),
            entity.getPInstructions(),
            entity.getPFollowupAt(),
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

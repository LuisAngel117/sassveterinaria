package com.sassveterinaria.clinical.service;

import com.sassveterinaria.clinical.domain.PrescriptionEntity;
import com.sassveterinaria.clinical.domain.VisitEntity;
import com.sassveterinaria.clinical.dto.PrescriptionCreateRequest;
import com.sassveterinaria.clinical.dto.PrescriptionPatchRequest;
import com.sassveterinaria.clinical.dto.PrescriptionResponse;
import com.sassveterinaria.clinical.repo.PrescriptionRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final VisitService visitService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository, VisitService visitService) {
        this.prescriptionRepository = prescriptionRepository;
        this.visitService = visitService;
    }

    @Transactional
    public PrescriptionResponse create(AuthPrincipal principal, UUID visitId, PrescriptionCreateRequest request) {
        VisitEntity visit = visitService.requireVisit(principal, visitId);
        visitService.requireVisitOpen(visit);

        PrescriptionEntity entity = new PrescriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setVisitId(visit.getId());
        entity.setMedication(required(request.medication(), "medication es requerido.", 160));
        entity.setDose(required(request.dose(), "dose es requerido.", 60));
        entity.setUnit(required(request.unit(), "unit es requerido.", 30));
        entity.setFrequency(required(request.frequency(), "frequency es requerido.", 60));
        entity.setDuration(required(request.duration(), "duration es requerido.", 60));
        entity.setRoute(required(request.route(), "route es requerido.", 60));
        entity.setNotes(normalizeText(request.notes()));
        entity.setCreatedAt(OffsetDateTime.now());

        return toResponse(prescriptionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> listByVisit(AuthPrincipal principal, UUID visitId) {
        visitService.requireVisit(principal, visitId);
        return prescriptionRepository
            .findByBranchIdAndVisitIdOrderByCreatedAtAsc(principal.getBranchId(), visitId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public PrescriptionResponse update(AuthPrincipal principal, UUID prescriptionId, PrescriptionPatchRequest request) {
        PrescriptionEntity entity = prescriptionRepository.findByIdAndBranchId(prescriptionId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/prescription-not-found",
                "Prescription not found",
                "No se encontro la prescripcion.",
                "PRESCRIPTION_NOT_FOUND"
            ));

        VisitEntity visit = visitService.requireVisit(principal, entity.getVisitId());
        visitService.requireVisitOpen(visit);

        if (request.medication() != null) {
            entity.setMedication(required(request.medication(), "medication no puede ser vacio.", 160));
        }
        if (request.dose() != null) {
            entity.setDose(required(request.dose(), "dose no puede ser vacio.", 60));
        }
        if (request.unit() != null) {
            entity.setUnit(required(request.unit(), "unit no puede ser vacio.", 30));
        }
        if (request.frequency() != null) {
            entity.setFrequency(required(request.frequency(), "frequency no puede ser vacio.", 60));
        }
        if (request.duration() != null) {
            entity.setDuration(required(request.duration(), "duration no puede ser vacio.", 60));
        }
        if (request.route() != null) {
            entity.setRoute(required(request.route(), "route no puede ser vacio.", 60));
        }
        if (request.notes() != null) {
            entity.setNotes(normalizeText(request.notes()));
        }

        return toResponse(prescriptionRepository.save(entity));
    }

    private String required(String value, String detail, int maxLen) {
        String normalized = normalizeText(value);
        if (normalized == null || normalized.length() > maxLen) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/prescription-validation",
                "Prescription validation error",
                detail,
                "PRESCRIPTION_VALIDATION_ERROR"
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

    private PrescriptionResponse toResponse(PrescriptionEntity entity) {
        return new PrescriptionResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getVisitId(),
            entity.getMedication(),
            entity.getDose(),
            entity.getUnit(),
            entity.getFrequency(),
            entity.getDuration(),
            entity.getRoute(),
            entity.getNotes(),
            entity.getCreatedAt()
        );
    }
}

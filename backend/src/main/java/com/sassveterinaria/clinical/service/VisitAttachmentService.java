package com.sassveterinaria.clinical.service;

import com.sassveterinaria.clinical.domain.VisitAttachmentEntity;
import com.sassveterinaria.clinical.domain.VisitEntity;
import com.sassveterinaria.clinical.dto.VisitAttachmentResponse;
import com.sassveterinaria.clinical.repo.VisitAttachmentRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VisitAttachmentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "application/pdf",
        "image/jpeg",
        "image/png"
    );

    private final VisitAttachmentRepository visitAttachmentRepository;
    private final VisitService visitService;
    private final VisitAttachmentStorageService storageService;
    private final VisitAttachmentProperties properties;

    public VisitAttachmentService(
        VisitAttachmentRepository visitAttachmentRepository,
        VisitService visitService,
        VisitAttachmentStorageService storageService,
        VisitAttachmentProperties properties
    ) {
        this.visitAttachmentRepository = visitAttachmentRepository;
        this.visitService = visitService;
        this.storageService = storageService;
        this.properties = properties;
    }

    @Transactional
    public VisitAttachmentResponse upload(AuthPrincipal principal, UUID visitId, MultipartFile file) {
        VisitEntity visit = visitService.requireVisit(principal, visitId);
        visitService.requireVisitOpen(visit);

        if (file == null || file.isEmpty()) {
            throw validation("Debe enviar un archivo.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw validation("Tipo de archivo no permitido.");
        }

        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/visit-attachment-size-exceeded",
                "Attachment size exceeded",
                "El archivo excede el tamano maximo permitido.",
                "VISIT_ATTACHMENT_SIZE_EXCEEDED"
            );
        }

        long total = visitAttachmentRepository.countByBranchIdAndVisitId(principal.getBranchId(), visitId);
        if (total >= properties.getMaxPerVisit()) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/visit-attachments-limit",
                "Attachments limit reached",
                "La visita excede el limite de adjuntos permitidos.",
                "VISIT_ATTACHMENTS_LIMIT"
            );
        }

        VisitAttachmentEntity entity = new VisitAttachmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setVisitId(visitId);
        entity.setOriginalFilename(file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename());
        entity.setContentType(contentType.toLowerCase());
        entity.setSizeBytes(file.getSize());
        entity.setCreatedBy(principal.getUserId());
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setStoragePath(storageService.store(visitId, entity.getId(), file));

        return toResponse(visitAttachmentRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<VisitAttachmentResponse> listByVisit(AuthPrincipal principal, UUID visitId) {
        visitService.requireVisit(principal, visitId);
        return visitAttachmentRepository
            .findByBranchIdAndVisitIdOrderByCreatedAtAsc(principal.getBranchId(), visitId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DownloadResult download(AuthPrincipal principal, UUID attachmentId) {
        VisitAttachmentEntity entity = requireAttachment(principal, attachmentId);
        Resource resource = storageService.load(entity.getStoragePath());
        return new DownloadResult(resource, entity.getOriginalFilename(), entity.getContentType());
    }

    @Transactional
    public void delete(AuthPrincipal principal, UUID attachmentId) {
        VisitAttachmentEntity entity = requireAttachment(principal, attachmentId);
        VisitEntity visit = visitService.requireVisit(principal, entity.getVisitId());
        visitService.requireVisitOpen(visit);
        visitAttachmentRepository.delete(entity);
        storageService.delete(entity.getStoragePath());
    }

    private VisitAttachmentEntity requireAttachment(AuthPrincipal principal, UUID attachmentId) {
        return visitAttachmentRepository.findByIdAndBranchId(attachmentId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/attachment-not-found",
                "Attachment not found",
                "No se encontro el adjunto.",
                "ATTACHMENT_NOT_FOUND"
            ));
    }

    private ApiProblemException validation(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/visit-attachment-validation",
            "Attachment validation error",
            detail,
            "VISIT_ATTACHMENT_VALIDATION_ERROR"
        );
    }

    private VisitAttachmentResponse toResponse(VisitAttachmentEntity entity) {
        return new VisitAttachmentResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getVisitId(),
            entity.getOriginalFilename(),
            entity.getContentType(),
            entity.getSizeBytes(),
            entity.getCreatedBy(),
            entity.getCreatedAt()
        );
    }

    public record DownloadResult(Resource resource, String originalFilename, String contentType) {
    }
}

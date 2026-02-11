package com.sassveterinaria.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sassveterinaria.audit.domain.AuditEventEntity;
import com.sassveterinaria.audit.dto.AuditEventResponse;
import com.sassveterinaria.audit.dto.AuditEventsPageResponse;
import com.sassveterinaria.audit.repo.AuditEventRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;
    private final int retentionDays;

    public AuditService(
        AuditEventRepository auditEventRepository,
        ObjectMapper objectMapper,
        @Value("${app.audit.retention-days:90}") int retentionDays
    ) {
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
        this.retentionDays = retentionDays;
    }

    @Transactional
    public void record(
        AuthPrincipal principal,
        String actionCode,
        String entityName,
        UUID entityId,
        String reason,
        Object beforePayload,
        Object afterPayload
    ) {
        boolean sensitive = reason != null && !reason.isBlank();
        persistEvent(
            principal.getUserId(),
            principal.getUsername(),
            principal.getBranchId(),
            actionCode,
            entityName,
            entityId,
            sensitive,
            normalizeOptionalReason(reason),
            beforePayload,
            afterPayload
        );
    }

    @Transactional
    public void recordEvent(
        AuthPrincipal principal,
        String actionCode,
        String entityType,
        UUID entityId,
        Object afterPayload
    ) {
        persistEvent(
            principal.getUserId(),
            principal.getUsername(),
            principal.getBranchId(),
            actionCode,
            entityType,
            entityId,
            false,
            null,
            null,
            afterPayload
        );
    }

    @Transactional
    public void recordSensitiveEvent(
        AuthPrincipal principal,
        String actionCode,
        String entityType,
        UUID entityId,
        String reason,
        Object beforePayload,
        Object afterPayload
    ) {
        String normalizedReason = normalizeSensitiveReason(reason);
        if (beforePayload == null || afterPayload == null) {
            throw validation("before/after es obligatorio en acciones sensibles.");
        }

        persistEvent(
            principal.getUserId(),
            principal.getUsername(),
            principal.getBranchId(),
            actionCode,
            entityType,
            entityId,
            true,
            normalizedReason,
            beforePayload,
            afterPayload
        );
    }

    @Transactional
    public void recordAuthEvent(
        UUID actorUserId,
        String actorUsername,
        UUID branchId,
        String actionCode,
        String entityType,
        UUID entityId,
        Object afterPayload
    ) {
        persistEvent(
            actorUserId,
            actorUsername,
            branchId,
            actionCode,
            entityType,
            entityId,
            false,
            null,
            null,
            afterPayload
        );
    }

    @Transactional(readOnly = true)
    public AuditEventsPageResponse search(
        AuthPrincipal principal,
        OffsetDateTime from,
        OffsetDateTime to,
        String action,
        String entityType,
        UUID entityId,
        UUID actorUserId,
        String actorUsername,
        Integer page,
        Integer size
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw validation("from no puede ser mayor que to.");
        }

        int resolvedPage = page == null ? 0 : page;
        int resolvedSize = size == null ? 20 : size;
        if (resolvedPage < 0) {
            throw validation("page debe ser >= 0.");
        }
        if (resolvedSize < 1 || resolvedSize > 100) {
            throw validation("size debe estar entre 1 y 100.");
        }

        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<AuditEventEntity> spec = branchScoped(principal.getBranchId())
            .and(createdAtFrom(from))
            .and(createdAtTo(to))
            .and(actionEq(normalizeFilter(action)))
            .and(entityTypeEq(normalizeFilter(entityType)))
            .and(entityIdEq(entityId))
            .and(actorUserIdEq(actorUserId))
            .and(actorUsernameContains(normalizeFilter(actorUsername)));

        Page<AuditEventEntity> result = auditEventRepository.findAll(spec, pageable);
        List<AuditEventResponse> items = result.getContent().stream().map(this::toResponse).toList();
        return new AuditEventsPageResponse(items, result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Transactional
    public long purgeExpiredEvents() {
        OffsetDateTime threshold = OffsetDateTime.now().minusDays(retentionDays);
        return purgeOlderThan(threshold);
    }

    @Transactional
    public long purgeOlderThan(OffsetDateTime threshold) {
        return auditEventRepository.deleteByCreatedAtBefore(threshold);
    }

    private void persistEvent(
        UUID actorUserId,
        String actorUsername,
        UUID branchId,
        String actionCode,
        String entityType,
        UUID entityId,
        boolean sensitive,
        String reason,
        Object beforePayload,
        Object afterPayload
    ) {
        AuditEventEntity event = new AuditEventEntity();
        event.setId(UUID.randomUUID());
        event.setCreatedAt(OffsetDateTime.now());
        event.setBranchId(branchId);
        event.setActorUserId(actorUserId);
        event.setActorUsername(normalizeOptionalFilter(actorUsername));
        event.setActionCode(actionCode);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setSensitive(sensitive);
        event.setReason(reason);
        event.setBeforeJson(toJson(beforePayload));
        event.setAfterJson(toJson(afterPayload));
        event.setIp(currentIp());
        event.setUserAgent(currentUserAgent());
        auditEventRepository.save(event);
    }

    private String toJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize audit payload", ex);
        }
    }

    private String normalizeSensitiveReason(String reason) {
        String normalized = normalizeOptionalReason(reason);
        if (normalized == null || normalized.length() < 10) {
            throw validation("reason es requerido (minimo 10 caracteres).");
        }
        return normalized;
    }

    private String normalizeOptionalReason(String reason) {
        if (reason == null) {
            return null;
        }
        String normalized = reason.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeOptionalFilter(String value) {
        return normalizeFilter(value);
    }

    private Specification<AuditEventEntity> branchScoped(UUID branchId) {
        return (root, query, cb) -> cb.equal(root.get("branchId"), branchId);
    }

    private Specification<AuditEventEntity> createdAtFrom(OffsetDateTime from) {
        return from == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    private Specification<AuditEventEntity> createdAtTo(OffsetDateTime to) {
        return to == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    private Specification<AuditEventEntity> actionEq(String action) {
        return action == null ? null : (root, query, cb) -> cb.equal(root.get("actionCode"), action);
    }

    private Specification<AuditEventEntity> entityTypeEq(String entityType) {
        return entityType == null ? null : (root, query, cb) -> cb.equal(root.get("entityType"), entityType);
    }

    private Specification<AuditEventEntity> entityIdEq(UUID entityId) {
        return entityId == null ? null : (root, query, cb) -> cb.equal(root.get("entityId"), entityId);
    }

    private Specification<AuditEventEntity> actorUserIdEq(UUID actorUserId) {
        return actorUserId == null ? null : (root, query, cb) -> cb.equal(root.get("actorUserId"), actorUserId);
    }

    private Specification<AuditEventEntity> actorUsernameContains(String actorUsername) {
        if (actorUsername == null) {
            return null;
        }
        String like = "%" + actorUsername.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("actorUsername")), like);
    }

    private AuditEventResponse toResponse(AuditEventEntity event) {
        return new AuditEventResponse(
            event.getId(),
            event.getCreatedAt(),
            event.getActorUserId(),
            event.getActorUsername(),
            event.getBranchId(),
            event.getActionCode(),
            event.getEntityType(),
            event.getEntityId(),
            event.isSensitive(),
            event.getReason(),
            event.getBeforeJson(),
            event.getAfterJson(),
            event.getIp(),
            event.getUserAgent()
        );
    }

    private String currentIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String forwarded = normalizeFilter(request.getHeader("X-Forwarded-For"));
        if (forwarded != null) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex >= 0 ? forwarded.substring(0, commaIndex).trim() : forwarded;
        }
        return normalizeFilter(request.getRemoteAddr());
    }

    private String currentUserAgent() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String userAgent = normalizeFilter(request.getHeader("User-Agent"));
        if (userAgent == null) {
            return null;
        }
        return userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent;
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        return servletAttributes.getRequest();
    }

    private ApiProblemException validation(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/audit-validation",
            "Audit validation error",
            detail,
            "AUDIT_VALIDATION_ERROR"
        );
    }
}

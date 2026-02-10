package com.sassveterinaria.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sassveterinaria.audit.domain.AuditEventEntity;
import com.sassveterinaria.audit.repo.AuditEventRepository;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    public void record(
        AuthPrincipal principal,
        String actionCode,
        String entityName,
        UUID entityId,
        String reason,
        Object beforePayload,
        Object afterPayload
    ) {
        AuditEventEntity event = new AuditEventEntity();
        event.setId(UUID.randomUUID());
        event.setBranchId(principal.getBranchId());
        event.setActorId(principal.getUserId());
        event.setActionCode(actionCode);
        event.setEntityName(entityName);
        event.setEntityId(entityId);
        event.setReason(reason);
        event.setBeforeJson(toJson(beforePayload));
        event.setAfterJson(toJson(afterPayload));
        event.setCreatedAt(OffsetDateTime.now());
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
}

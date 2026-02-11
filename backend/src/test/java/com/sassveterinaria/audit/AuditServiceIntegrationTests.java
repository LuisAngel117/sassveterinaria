package com.sassveterinaria.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sassveterinaria.audit.domain.AuditEventEntity;
import com.sassveterinaria.audit.repo.AuditEventRepository;
import com.sassveterinaria.audit.service.AuditService;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuditServiceIntegrationTests {

    private static final UUID BRANCH_ID = UUID.fromString("f9e4602d-f044-49ad-8eaf-e01efce8fcf4");
    private static final UUID USER_ID = UUID.fromString("e7a521cc-84a5-4682-9f8d-87a28fd3f5cd");

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    void recordEventStoresAuditEntry() {
        UUID entityId = UUID.randomUUID();
        auditService.recordEvent(principal(), "APPT_CREATE", "appointment", entityId, Map.of("status", "RESERVED"));

        AuditEventEntity saved = auditEventRepository.findTopByActionCodeOrderByCreatedAtDesc("APPT_CREATE").orElseThrow();
        assertEquals(USER_ID, saved.getActorUserId());
        assertEquals("admin@sassvet.local", saved.getActorUsername());
        assertEquals(BRANCH_ID, saved.getBranchId());
        assertEquals(entityId, saved.getEntityId());
        assertFalse(saved.isSensitive());
        assertNotNull(saved.getAfterJson());
    }

    @Test
    void recordSensitiveEventStoresBeforeAfterAndReason() {
        UUID entityId = UUID.randomUUID();
        auditService.recordSensitiveEvent(
            principal(),
            "CONFIG_TAX_UPDATE",
            "tax_config",
            entityId,
            "Actualizacion anual IVA",
            Map.of("taxRate", "0.1200"),
            Map.of("taxRate", "0.1500")
        );

        AuditEventEntity saved = auditEventRepository.findTopByActionCodeOrderByCreatedAtDesc("CONFIG_TAX_UPDATE").orElseThrow();
        assertTrue(saved.isSensitive());
        assertEquals("Actualizacion anual IVA", saved.getReason());
        assertTrue(saved.getBeforeJson().contains("0.1200"));
        assertTrue(saved.getAfterJson().contains("0.1500"));
    }

    @Test
    void purgeOlderThanRemovesOldEventsAndKeepsRecent() {
        AuditEventEntity oldEvent = buildEvent("TEST_OLD", OffsetDateTime.now().minusDays(120));
        AuditEventEntity recentEvent = buildEvent("TEST_RECENT", OffsetDateTime.now().minusDays(2));
        auditEventRepository.save(oldEvent);
        auditEventRepository.save(recentEvent);

        long deleted = auditService.purgeOlderThan(OffsetDateTime.now().minusDays(90));

        assertEquals(1L, deleted);
        assertTrue(auditEventRepository.findById(oldEvent.getId()).isEmpty());
        assertTrue(auditEventRepository.findById(recentEvent.getId()).isPresent());
    }

    private AuthPrincipal principal() {
        return new AuthPrincipal(
            USER_ID,
            "admin@sassvet.local",
            "Admin Demo",
            "ADMIN",
            BRANCH_ID,
            List.of("AUDIT_READ")
        );
    }

    private AuditEventEntity buildEvent(String actionCode, OffsetDateTime createdAt) {
        AuditEventEntity event = new AuditEventEntity();
        event.setId(UUID.randomUUID());
        event.setBranchId(BRANCH_ID);
        event.setActorUserId(USER_ID);
        event.setActorUsername("admin@sassvet.local");
        event.setActionCode(actionCode);
        event.setEntityType("test_entity");
        event.setEntityId(UUID.randomUUID());
        event.setSensitive(false);
        event.setReason(null);
        event.setBeforeJson(null);
        event.setAfterJson("{\"ok\":true}");
        event.setIp("127.0.0.1");
        event.setUserAgent("JUnit");
        event.setCreatedAt(createdAt);
        return event;
    }
}

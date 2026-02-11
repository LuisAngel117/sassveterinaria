package com.sassveterinaria.audit.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditRetentionScheduler {

    private final AuditService auditService;

    public AuditRetentionScheduler(AuditService auditService) {
        this.auditService = auditService;
    }

    @Scheduled(cron = "${app.audit.purge-cron:0 30 3 * * *}")
    public void purgeExpiredAuditEvents() {
        auditService.purgeExpiredEvents();
    }
}

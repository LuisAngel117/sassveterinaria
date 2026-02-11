package com.sassveterinaria.audit.repo;

import com.sassveterinaria.audit.domain.AuditEventEntity;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID>, JpaSpecificationExecutor<AuditEventEntity> {
    long deleteByCreatedAtBefore(OffsetDateTime threshold);

    Optional<AuditEventEntity> findTopByActionCodeOrderByCreatedAtDesc(String actionCode);
}

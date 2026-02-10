package com.sassveterinaria.audit.repo;

import com.sassveterinaria.audit.domain.AuditEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {
}

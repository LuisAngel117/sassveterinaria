package com.sassveterinaria.clinical.repo;

import com.sassveterinaria.clinical.domain.VisitAttachmentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitAttachmentRepository extends JpaRepository<VisitAttachmentEntity, UUID> {
    Optional<VisitAttachmentEntity> findByIdAndBranchId(UUID id, UUID branchId);

    List<VisitAttachmentEntity> findByBranchIdAndVisitIdOrderByCreatedAtAsc(UUID branchId, UUID visitId);

    long countByBranchIdAndVisitId(UUID branchId, UUID visitId);
}

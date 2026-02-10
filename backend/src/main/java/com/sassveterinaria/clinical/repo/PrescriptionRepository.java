package com.sassveterinaria.clinical.repo;

import com.sassveterinaria.clinical.domain.PrescriptionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<PrescriptionEntity, UUID> {
    Optional<PrescriptionEntity> findByIdAndBranchId(UUID id, UUID branchId);

    List<PrescriptionEntity> findByBranchIdAndVisitIdOrderByCreatedAtAsc(UUID branchId, UUID visitId);
}

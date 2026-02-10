package com.sassveterinaria.clinical.repo;

import com.sassveterinaria.clinical.domain.SoapTemplateEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoapTemplateRepository extends JpaRepository<SoapTemplateEntity, UUID> {
    Optional<SoapTemplateEntity> findByIdAndBranchId(UUID id, UUID branchId);

    List<SoapTemplateEntity> findByBranchIdAndServiceIdOrderByNameAsc(UUID branchId, UUID serviceId);
}

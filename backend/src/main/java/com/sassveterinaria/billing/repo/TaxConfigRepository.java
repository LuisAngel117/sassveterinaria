package com.sassveterinaria.billing.repo;

import com.sassveterinaria.billing.domain.TaxConfigEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxConfigRepository extends JpaRepository<TaxConfigEntity, UUID> {
    Optional<TaxConfigEntity> findByBranchId(UUID branchId);
}

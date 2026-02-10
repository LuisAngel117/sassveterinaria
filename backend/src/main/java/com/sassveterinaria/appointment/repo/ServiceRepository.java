package com.sassveterinaria.appointment.repo;

import com.sassveterinaria.appointment.domain.ServiceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByBranchIdAndIsActiveTrueOrderByNameAsc(UUID branchId);

    Optional<ServiceEntity> findByIdAndBranchIdAndIsActiveTrue(UUID id, UUID branchId);
}

package com.sassveterinaria.inventory.repo;

import com.sassveterinaria.inventory.domain.ServiceBomItemEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceBomItemRepository extends JpaRepository<ServiceBomItemEntity, UUID> {
    List<ServiceBomItemEntity> findByBranchIdAndServiceIdOrderByProductIdAsc(UUID branchId, UUID serviceId);

    boolean existsByBranchIdAndServiceId(UUID branchId, UUID serviceId);

    void deleteByBranchIdAndServiceId(UUID branchId, UUID serviceId);
}

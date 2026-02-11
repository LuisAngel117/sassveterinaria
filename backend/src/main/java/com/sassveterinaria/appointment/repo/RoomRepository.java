package com.sassveterinaria.appointment.repo;

import com.sassveterinaria.appointment.domain.RoomEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {
    List<RoomEntity> findByBranchIdAndIsActiveTrueOrderByNameAsc(UUID branchId);

    Optional<RoomEntity> findByIdAndBranchIdAndIsActiveTrue(UUID id, UUID branchId);

    List<RoomEntity> findByBranchIdAndIdIn(UUID branchId, List<UUID> ids);
}

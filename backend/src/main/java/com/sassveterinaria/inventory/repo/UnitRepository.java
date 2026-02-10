package com.sassveterinaria.inventory.repo;

import com.sassveterinaria.inventory.domain.UnitEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<UnitEntity, UUID> {
    List<UnitEntity> findByIsActiveTrueOrderByNameAsc();

    List<UnitEntity> findByIdIn(Collection<UUID> ids);

    Optional<UnitEntity> findByIdAndIsActiveTrue(UUID id);

    Optional<UnitEntity> findByCodeIgnoreCase(String code);
}

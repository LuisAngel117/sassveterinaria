package com.sassveterinaria.crm.repo;

import com.sassveterinaria.crm.domain.PetEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PetRepository extends JpaRepository<PetEntity, UUID> {
    Optional<PetEntity> findByIdAndBranchId(UUID id, UUID branchId);

    List<PetEntity> findByBranchIdAndIdIn(UUID branchId, List<UUID> ids);

    List<PetEntity> findByBranchIdAndClientIdOrderByNameAsc(UUID branchId, UUID clientId);

    long countByBranchId(UUID branchId);

    boolean existsByBranchIdAndInternalCodeIgnoreCase(UUID branchId, String internalCode);

    @Query("""
        SELECT (COUNT(p) > 0)
        FROM PetEntity p
        WHERE p.branchId = :branchId
          AND LOWER(p.internalCode) = LOWER(:internalCode)
          AND p.id <> :petId
        """)
    boolean existsByBranchIdAndInternalCodeIgnoreCaseAndIdNot(
        @Param("branchId") UUID branchId,
        @Param("internalCode") String internalCode,
        @Param("petId") UUID petId
    );
}

package com.sassveterinaria.clinical.repo;

import com.sassveterinaria.clinical.domain.VisitEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitRepository extends JpaRepository<VisitEntity, UUID> {
    Optional<VisitEntity> findByIdAndBranchId(UUID id, UUID branchId);

    @Query("""
        SELECT v
        FROM VisitEntity v
        WHERE v.branchId = :branchId
          AND v.petId = :petId
          AND (:status IS NULL OR v.status = :status)
          AND (:from IS NULL OR v.createdAt >= :from)
          AND (:to IS NULL OR v.createdAt <= :to)
        ORDER BY v.createdAt DESC
        """)
    List<VisitEntity> searchByPet(
        @Param("branchId") UUID branchId,
        @Param("petId") UUID petId,
        @Param("status") String status,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to
    );
}

package com.sassveterinaria.appointment.repo;

import com.sassveterinaria.appointment.domain.ServiceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    Optional<ServiceEntity> findByIdAndBranchIdAndIsActiveTrue(UUID id, UUID branchId);

    Optional<ServiceEntity> findByIdAndBranchId(UUID id, UUID branchId);

    long countByBranchId(UUID branchId);

    boolean existsByBranchIdAndNameIgnoreCase(UUID branchId, String name);

    @Query("""
        SELECT (COUNT(s) > 0)
        FROM ServiceEntity s
        WHERE s.branchId = :branchId
          AND LOWER(s.name) = LOWER(:name)
          AND s.id <> :serviceId
        """)
    boolean existsByBranchIdAndNameIgnoreCaseAndIdNot(
        @Param("branchId") UUID branchId,
        @Param("name") String name,
        @Param("serviceId") UUID serviceId
    );

    @Query("""
        SELECT s
        FROM ServiceEntity s
        WHERE s.branchId = :branchId
          AND (:active IS NULL OR s.isActive = :active)
          AND (
            :q IS NULL
            OR TRIM(:q) = ''
            OR LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY s.name ASC
        """)
    List<ServiceEntity> search(
        @Param("branchId") UUID branchId,
        @Param("active") Boolean active,
        @Param("q") String q
    );
}

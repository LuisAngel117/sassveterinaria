package com.sassveterinaria.inventory.repo;

import com.sassveterinaria.inventory.domain.StockMovementEntity;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {
    @Query("""
        SELECT m
        FROM StockMovementEntity m
        WHERE m.branchId = :branchId
          AND (:productId IS NULL OR m.productId = :productId)
          AND (:from IS NULL OR m.createdAt >= :from)
          AND (:to IS NULL OR m.createdAt <= :to)
          AND (:type IS NULL OR m.type = :type)
        ORDER BY m.createdAt DESC
        """)
    List<StockMovementEntity> search(
        @Param("branchId") UUID branchId,
        @Param("productId") UUID productId,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        @Param("type") String type
    );

    @Query("""
        SELECT m
        FROM StockMovementEntity m
        WHERE m.branchId = :branchId
          AND m.createdAt >= :from
          AND m.createdAt <= :to
          AND m.type IN :types
          AND (:productId IS NULL OR m.productId = :productId)
        ORDER BY m.createdAt ASC
        """)
    List<StockMovementEntity> findForConsumptionReport(
        @Param("branchId") UUID branchId,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        @Param("types") Collection<String> types,
        @Param("productId") UUID productId
    );
}

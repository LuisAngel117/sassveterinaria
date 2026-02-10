package com.sassveterinaria.inventory.repo;

import com.sassveterinaria.inventory.domain.ProductStockEntity;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductStockRepository extends JpaRepository<ProductStockEntity, UUID> {
    Optional<ProductStockEntity> findByBranchIdAndProductId(UUID branchId, UUID productId);

    List<ProductStockEntity> findByBranchIdAndProductIdIn(UUID branchId, Collection<UUID> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM ProductStockEntity s
        WHERE s.branchId = :branchId
          AND s.productId = :productId
        """)
    Optional<ProductStockEntity> findByBranchIdAndProductIdForUpdate(
        @Param("branchId") UUID branchId,
        @Param("productId") UUID productId
    );
}

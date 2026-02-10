package com.sassveterinaria.inventory.repo;

import com.sassveterinaria.inventory.domain.ProductEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Optional<ProductEntity> findByIdAndBranchId(UUID id, UUID branchId);

    List<ProductEntity> findByBranchIdAndIdIn(UUID branchId, Collection<UUID> ids);

    Optional<ProductEntity> findByBranchIdAndSkuIgnoreCase(UUID branchId, String sku);

    Optional<ProductEntity> findByBranchIdAndNameIgnoreCase(UUID branchId, String name);

    @Query("""
        SELECT p
        FROM ProductEntity p
        LEFT JOIN ProductStockEntity s ON s.productId = p.id AND s.branchId = p.branchId
        WHERE p.branchId = :branchId
          AND (:active IS NULL OR p.isActive = :active)
          AND (
            :q IS NULL
            OR TRIM(:q) = ''
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(COALESCE(p.sku, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
          AND (
            :lowStock IS NULL
            OR (
              :lowStock = TRUE
              AND COALESCE(s.onHandQty, 0) <= p.minQty
            )
            OR (
              :lowStock = FALSE
              AND COALESCE(s.onHandQty, 0) > p.minQty
            )
          )
        ORDER BY p.name ASC
        """)
    List<ProductEntity> search(
        @Param("branchId") UUID branchId,
        @Param("q") String q,
        @Param("active") Boolean active,
        @Param("lowStock") Boolean lowStock
    );
}

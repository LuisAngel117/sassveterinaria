package com.sassveterinaria.billing.repo;

import com.sassveterinaria.billing.domain.InvoiceItemEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItemEntity, UUID> {
    Optional<InvoiceItemEntity> findByIdAndBranchId(UUID id, UUID branchId);

    List<InvoiceItemEntity> findByBranchIdAndInvoiceIdOrderByCreatedAtAsc(UUID branchId, UUID invoiceId);

    @Query("""
        SELECT COALESCE(SUM(i.qty), 0)
        FROM InvoiceItemEntity i
        WHERE i.branchId = :branchId
          AND i.invoiceId = :invoiceId
          AND i.itemType = 'PRODUCT'
          AND i.itemId = :productId
        """)
    BigDecimal sumProductQty(
        @Param("branchId") UUID branchId,
        @Param("invoiceId") UUID invoiceId,
        @Param("productId") UUID productId
    );

    @Query("""
        SELECT COALESCE(SUM(i.qty), 0)
        FROM InvoiceItemEntity i
        WHERE i.branchId = :branchId
          AND i.invoiceId = :invoiceId
          AND i.itemType = 'PRODUCT'
          AND i.itemId = :productId
          AND i.id <> :itemId
        """)
    BigDecimal sumProductQtyExcludingItem(
        @Param("branchId") UUID branchId,
        @Param("invoiceId") UUID invoiceId,
        @Param("productId") UUID productId,
        @Param("itemId") UUID itemId
    );
}

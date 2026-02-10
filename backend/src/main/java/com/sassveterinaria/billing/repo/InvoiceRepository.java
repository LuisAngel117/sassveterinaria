package com.sassveterinaria.billing.repo;

import com.sassveterinaria.billing.domain.InvoiceEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
    Optional<InvoiceEntity> findByIdAndBranchId(UUID id, UUID branchId);

    @Query("""
        SELECT i
        FROM InvoiceEntity i
        WHERE i.branchId = :branchId
          AND (:status IS NULL OR i.status = :status)
          AND (:from IS NULL OR i.createdAt >= :from)
          AND (:to IS NULL OR i.createdAt <= :to)
          AND (
            :q IS NULL
            OR TRIM(:q) = ''
            OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY i.createdAt DESC
        """)
    List<InvoiceEntity> search(
        @Param("branchId") UUID branchId,
        @Param("status") String status,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        @Param("q") String q
    );
}

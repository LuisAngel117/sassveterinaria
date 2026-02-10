package com.sassveterinaria.billing.repo;

import com.sassveterinaria.billing.domain.InvoicePaymentEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoicePaymentRepository extends JpaRepository<InvoicePaymentEntity, UUID> {
    List<InvoicePaymentEntity> findByBranchIdAndInvoiceIdOrderByCreatedAtAsc(UUID branchId, UUID invoiceId);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM InvoicePaymentEntity p
        WHERE p.branchId = :branchId
          AND p.invoiceId = :invoiceId
        """)
    BigDecimal sumAmountByInvoice(
        @Param("branchId") UUID branchId,
        @Param("invoiceId") UUID invoiceId
    );
}

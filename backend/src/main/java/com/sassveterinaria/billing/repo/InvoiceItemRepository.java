package com.sassveterinaria.billing.repo;

import com.sassveterinaria.billing.domain.InvoiceItemEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItemEntity, UUID> {
    Optional<InvoiceItemEntity> findByIdAndBranchId(UUID id, UUID branchId);

    List<InvoiceItemEntity> findByBranchIdAndInvoiceIdOrderByCreatedAtAsc(UUID branchId, UUID invoiceId);
}

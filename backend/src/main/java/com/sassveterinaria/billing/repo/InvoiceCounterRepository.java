package com.sassveterinaria.billing.repo;

import com.sassveterinaria.billing.domain.InvoiceCounterEntity;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceCounterRepository extends JpaRepository<InvoiceCounterEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM InvoiceCounterEntity c WHERE c.branchId = :branchId")
    Optional<InvoiceCounterEntity> findByBranchIdForUpdate(@Param("branchId") UUID branchId);
}

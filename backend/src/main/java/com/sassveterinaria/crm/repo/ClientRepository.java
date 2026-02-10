package com.sassveterinaria.crm.repo;

import com.sassveterinaria.crm.domain.ClientEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {
    Optional<ClientEntity> findByIdAndBranchId(UUID id, UUID branchId);

    long countByBranchId(UUID branchId);

    @Query("""
        SELECT c
        FROM ClientEntity c
        WHERE c.branchId = :branchId
          AND (
            :q IS NULL
            OR TRIM(:q) = ''
            OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(COALESCE(c.phone, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(COALESCE(c.identification, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """)
    Page<ClientEntity> search(@Param("branchId") UUID branchId, @Param("q") String q, Pageable pageable);
}

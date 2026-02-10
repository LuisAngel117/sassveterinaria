package com.sassveterinaria.auth.repo;

import com.sassveterinaria.auth.domain.BranchEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<BranchEntity, UUID> {
}

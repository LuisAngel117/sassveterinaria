package com.sassveterinaria.auth.repo;

import com.sassveterinaria.auth.domain.UserBranchEntity;
import com.sassveterinaria.auth.domain.UserBranchId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBranchRepository extends JpaRepository<UserBranchEntity, UserBranchId> {
    Optional<UserBranchEntity> findFirstByIdUserIdAndIsDefaultTrue(UUID userId);

    List<UserBranchEntity> findByIdUserId(UUID userId);
}

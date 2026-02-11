package com.sassveterinaria.auth.repo;

import com.sassveterinaria.auth.domain.AuthLoginAttemptEntity;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthLoginAttemptRepository extends JpaRepository<AuthLoginAttemptEntity, UUID> {
    long countByUserIdAndSuccessfulFalseAndCreatedAtGreaterThanEqual(UUID userId, OffsetDateTime from);

    long deleteByCreatedAtBefore(OffsetDateTime threshold);
}

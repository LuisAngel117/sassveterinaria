package com.sassveterinaria.auth.repo;

import com.sassveterinaria.auth.domain.RefreshTokenEntity;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    default Optional<RefreshTokenEntity> findActiveByTokenHash(String tokenHash, OffsetDateTime now) {
        return findByTokenHash(tokenHash)
            .filter(token -> token.getRevokedAt() == null)
            .filter(token -> token.getExpiresAt().isAfter(now));
    }
}

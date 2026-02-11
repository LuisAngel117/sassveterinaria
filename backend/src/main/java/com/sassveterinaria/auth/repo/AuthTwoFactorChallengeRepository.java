package com.sassveterinaria.auth.repo;

import com.sassveterinaria.auth.domain.AuthTwoFactorChallengeEntity;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTwoFactorChallengeRepository extends JpaRepository<AuthTwoFactorChallengeEntity, UUID> {
    Optional<AuthTwoFactorChallengeEntity> findByChallengeHash(String challengeHash);

    default Optional<AuthTwoFactorChallengeEntity> findActiveByChallengeHash(String challengeHash, OffsetDateTime now) {
        return findByChallengeHash(challengeHash)
            .filter(item -> item.getConsumedAt() == null)
            .filter(item -> item.getExpiresAt().isAfter(now));
    }

    long deleteByExpiresAtBefore(OffsetDateTime threshold);
}

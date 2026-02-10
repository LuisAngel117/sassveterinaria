package com.sassveterinaria.auth.repo;

import com.sassveterinaria.auth.domain.AppUserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {
    Optional<AppUserEntity> findByEmail(String email);
}

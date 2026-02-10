package com.sassveterinaria.bootstrap;

import com.sassveterinaria.auth.domain.AppUserEntity;
import com.sassveterinaria.auth.domain.BranchEntity;
import com.sassveterinaria.auth.domain.UserBranchEntity;
import com.sassveterinaria.auth.domain.UserBranchId;
import com.sassveterinaria.auth.repo.AppUserRepository;
import com.sassveterinaria.auth.repo.BranchRepository;
import com.sassveterinaria.auth.repo.UserBranchRepository;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataSeeder implements ApplicationRunner {

    private final BranchRepository branchRepository;
    private final AppUserRepository appUserRepository;
    private final UserBranchRepository userBranchRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
        BranchRepository branchRepository,
        AppUserRepository appUserRepository,
        UserBranchRepository userBranchRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.branchRepository = branchRepository;
        this.appUserRepository = appUserRepository;
        this.userBranchRepository = userBranchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (appUserRepository.count() > 0) {
            return;
        }

        BranchEntity branch = new BranchEntity();
        branch.setId(stableUuid("branch-centro"));
        branch.setCode("CENTRO");
        branch.setName("Sucursal Centro");
        branch.setActive(true);
        branch.setCreatedAt(OffsetDateTime.now());
        branchRepository.save(branch);

        createUser("superadmin", "Super Admin", "SUPERADMIN", "SuperAdmin123!", branch.getId());
        createUser("admin", "Admin", "ADMIN", "Admin123!", branch.getId());
        createUser("recepcion", "Recepcion", "RECEPCION", "Recepcion123!", branch.getId());
        createUser("veterinario", "Veterinario", "VETERINARIO", "Veterinario123!", branch.getId());
    }

    private void createUser(String username, String fullName, String roleCode, String password, UUID branchId) {
        AppUserEntity user = new AppUserEntity();
        user.setId(stableUuid("user-" + username));
        user.setEmail(username);
        user.setFullName(fullName);
        user.setRoleCode(roleCode);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setActive(true);
        user.setLockedUntil(null);
        user.setCreatedAt(OffsetDateTime.now());
        appUserRepository.save(user);

        UserBranchEntity userBranch = new UserBranchEntity();
        userBranch.setId(new UserBranchId(user.getId(), branchId));
        userBranch.setDefault(true);
        userBranchRepository.save(userBranch);
    }

    private UUID stableUuid(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}

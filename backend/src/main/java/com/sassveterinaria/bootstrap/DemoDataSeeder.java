package com.sassveterinaria.bootstrap;

import com.sassveterinaria.auth.domain.AppUserEntity;
import com.sassveterinaria.auth.domain.BranchEntity;
import com.sassveterinaria.auth.domain.UserBranchEntity;
import com.sassveterinaria.auth.domain.UserBranchId;
import com.sassveterinaria.auth.repo.AppUserRepository;
import com.sassveterinaria.auth.repo.BranchRepository;
import com.sassveterinaria.auth.repo.UserBranchRepository;
import com.sassveterinaria.appointment.domain.ServiceEntity;
import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.billing.domain.TaxConfigEntity;
import com.sassveterinaria.billing.repo.TaxConfigRepository;
import com.sassveterinaria.crm.domain.ClientEntity;
import com.sassveterinaria.crm.domain.PetEntity;
import com.sassveterinaria.crm.repo.ClientRepository;
import com.sassveterinaria.crm.repo.PetRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
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
    private final ServiceRepository serviceRepository;
    private final TaxConfigRepository taxConfigRepository;
    private final ClientRepository clientRepository;
    private final PetRepository petRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
        BranchRepository branchRepository,
        AppUserRepository appUserRepository,
        UserBranchRepository userBranchRepository,
        ServiceRepository serviceRepository,
        TaxConfigRepository taxConfigRepository,
        ClientRepository clientRepository,
        PetRepository petRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.branchRepository = branchRepository;
        this.appUserRepository = appUserRepository;
        this.userBranchRepository = userBranchRepository;
        this.serviceRepository = serviceRepository;
        this.taxConfigRepository = taxConfigRepository;
        this.clientRepository = clientRepository;
        this.petRepository = petRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        BranchEntity branch = ensureDemoBranch();

        if (appUserRepository.count() == 0) {
            createUser("superadmin", "Super Admin", "SUPERADMIN", "SuperAdmin123!", branch.getId());
            createUser("admin", "Admin", "ADMIN", "Admin123!", branch.getId());
            createUser("recepcion", "Recepcion", "RECEPCION", "Recepcion123!", branch.getId());
            createUser("veterinario", "Veterinario", "VETERINARIO", "Veterinario123!", branch.getId());
        }

        ensureDemoServices(branch.getId());
        ensureTaxConfig(branch.getId());
        ensureDemoClientAndPet(branch.getId());
    }

    private BranchEntity ensureDemoBranch() {
        UUID branchId = stableUuid("branch-centro");
        return branchRepository.findById(branchId).orElseGet(() -> {
            BranchEntity branch = new BranchEntity();
            branch.setId(branchId);
            branch.setCode("CENTRO");
            branch.setName("Sucursal Centro");
            branch.setActive(true);
            branch.setCreatedAt(OffsetDateTime.now());
            return branchRepository.save(branch);
        });
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

    private void ensureDemoClientAndPet(UUID branchId) {
        if (clientRepository.countByBranchId(branchId) > 0) {
            return;
        }

        ClientEntity client = new ClientEntity();
        client.setId(stableUuid("client-demo-1"));
        client.setBranchId(branchId);
        client.setFullName("Luis Demo");
        client.setIdentification("0102030405");
        client.setPhone("0990000001");
        client.setEmail("demo.cliente@example.com");
        client.setAddress("Av. Demo 123");
        client.setNotes("Cliente seed para smoke CRM.");
        client.setCreatedAt(OffsetDateTime.now());
        ClientEntity savedClient = clientRepository.save(client);

        if (petRepository.countByBranchId(branchId) > 0) {
            return;
        }

        PetEntity pet = new PetEntity();
        pet.setId(stableUuid("pet-demo-1"));
        pet.setBranchId(branchId);
        pet.setClientId(savedClient.getId());
        pet.setInternalCode("PET-DEMO-001");
        pet.setName("Milo");
        pet.setSpecies("Canino");
        pet.setBreed("Mestizo");
        pet.setSex("M");
        pet.setBirthDate(null);
        pet.setWeightKg(null);
        pet.setNeutered(Boolean.TRUE);
        pet.setAlerts("Ninguna");
        pet.setHistory("Paciente demo para smoke.");
        pet.setCreatedAt(OffsetDateTime.now());
        petRepository.save(pet);
    }

    private void ensureDemoServices(UUID branchId) {
        ensureService(branchId, "service-demo-consulta-general", "Consulta general", 30, new BigDecimal("20.00"));
        ensureService(branchId, "service-demo-vacunacion", "Vacunacion", 20, new BigDecimal("15.00"));
        ensureService(branchId, "service-demo-control-post", "Control post-operatorio", 30, new BigDecimal("18.00"));
    }

    private void ensureTaxConfig(UUID branchId) {
        if (taxConfigRepository.findByBranchId(branchId).isPresent()) {
            return;
        }
        AppUserEntity actor = appUserRepository.findByEmail("superadmin").orElse(null);
        if (actor == null) {
            return;
        }
        TaxConfigEntity config = new TaxConfigEntity();
        config.setId(stableUuid("tax-config-" + branchId));
        config.setBranchId(branchId);
        config.setTaxRate(new BigDecimal("0.1500"));
        config.setUpdatedBy(actor.getId());
        config.setUpdatedAt(OffsetDateTime.now());
        taxConfigRepository.save(config);
    }

    private void ensureService(UUID branchId, String seed, String name, int durationMinutes, BigDecimal priceBase) {
        if (serviceRepository.existsByBranchIdAndNameIgnoreCase(branchId, name)) {
            return;
        }

        ServiceEntity service = new ServiceEntity();
        service.setId(stableUuid(seed));
        service.setBranchId(branchId);
        service.setName(name);
        service.setDurationMinutes(durationMinutes);
        service.setPriceBase(priceBase);
        service.setActive(true);
        service.setCreatedAt(OffsetDateTime.now());
        serviceRepository.save(service);
    }

    private UUID stableUuid(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}

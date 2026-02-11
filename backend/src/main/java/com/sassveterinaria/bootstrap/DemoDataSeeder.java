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
import com.sassveterinaria.inventory.domain.ProductEntity;
import com.sassveterinaria.inventory.domain.ProductStockEntity;
import com.sassveterinaria.inventory.domain.ServiceBomItemEntity;
import com.sassveterinaria.inventory.domain.UnitEntity;
import com.sassveterinaria.inventory.repo.ProductRepository;
import com.sassveterinaria.inventory.repo.ProductStockRepository;
import com.sassveterinaria.inventory.repo.ServiceBomItemRepository;
import com.sassveterinaria.inventory.repo.UnitRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;
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
    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final ServiceBomItemRepository serviceBomItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
        BranchRepository branchRepository,
        AppUserRepository appUserRepository,
        UserBranchRepository userBranchRepository,
        ServiceRepository serviceRepository,
        TaxConfigRepository taxConfigRepository,
        ClientRepository clientRepository,
        PetRepository petRepository,
        UnitRepository unitRepository,
        ProductRepository productRepository,
        ProductStockRepository productStockRepository,
        ServiceBomItemRepository serviceBomItemRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.branchRepository = branchRepository;
        this.appUserRepository = appUserRepository;
        this.userBranchRepository = userBranchRepository;
        this.serviceRepository = serviceRepository;
        this.taxConfigRepository = taxConfigRepository;
        this.clientRepository = clientRepository;
        this.petRepository = petRepository;
        this.unitRepository = unitRepository;
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
        this.serviceBomItemRepository = serviceBomItemRepository;
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
        ensureInventorySeeds(branch.getId());
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
        user.setTotpSecret(null);
        user.setTotpEnabled(false);
        user.setTotpVerifiedAt(null);
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

    private void ensureInventorySeeds(UUID branchId) {
        UnitEntity unitUN = ensureUnit("unit-un", "UN", "Unidad");
        UnitEntity unitML = ensureUnit("unit-ml", "ML", "Mililitro");
        UnitEntity unitTablet = ensureUnit("unit-tablet", "TABLETA", "Tableta");
        UnitEntity unitAmpoule = ensureUnit("unit-ampoule", "AMPOLLA", "Ampolla");

        ProductEntity vacuna = ensureProduct(
            branchId,
            "product-vacuna-rabia",
            "VAC-RAB-001",
            "Vacuna antirrabica",
            unitUN.getId(),
            new BigDecimal("2.000"),
            new BigDecimal("25.000"),
            new BigDecimal("8.5000")
        );
        ProductEntity jeringa = ensureProduct(
            branchId,
            "product-jeringa-3ml",
            "INS-JER-003",
            "Jeringa 3ml",
            unitUN.getId(),
            new BigDecimal("20.000"),
            new BigDecimal("150.000"),
            new BigDecimal("0.3500")
        );
        ensureProduct(
            branchId,
            "product-suero-500ml",
            "INS-SUE-500",
            "Suero fisiologico 500ml",
            unitML.getId(),
            new BigDecimal("500.000"),
            new BigDecimal("4000.000"),
            new BigDecimal("0.0120")
        );
        ensureProduct(
            branchId,
            "product-antibiotico-tab",
            "MED-ATB-010",
            "Antibiotico tableta",
            unitTablet.getId(),
            new BigDecimal("30.000"),
            new BigDecimal("200.000"),
            new BigDecimal("0.4500")
        );
        ensureProduct(
            branchId,
            "product-analgesico-amp",
            "MED-ANL-001",
            "Analgesico ampolla",
            unitAmpoule.getId(),
            new BigDecimal("10.000"),
            new BigDecimal("50.000"),
            new BigDecimal("1.2000")
        );

        ensureBomForVaccination(branchId, vacuna.getId(), jeringa.getId());
    }

    private UnitEntity ensureUnit(String seed, String code, String name) {
        return unitRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
            UnitEntity unit = new UnitEntity();
            unit.setId(stableUuid(seed));
            unit.setCode(code);
            unit.setName(name);
            unit.setActive(true);
            return unitRepository.save(unit);
        });
    }

    private ProductEntity ensureProduct(
        UUID branchId,
        String seed,
        String sku,
        String name,
        UUID unitId,
        BigDecimal minQty,
        BigDecimal initialQty,
        BigDecimal avgCost
    ) {
        Optional<ProductEntity> existingBySku = productRepository.findByBranchIdAndSkuIgnoreCase(branchId, sku);
        ProductEntity product = existingBySku.orElseGet(() -> {
            ProductEntity created = new ProductEntity();
            created.setId(stableUuid(seed));
            created.setBranchId(branchId);
            created.setSku(sku);
            created.setName(name);
            created.setUnitId(unitId);
            created.setMinQty(minQty);
            created.setActive(true);
            created.setCreatedAt(OffsetDateTime.now());
            return productRepository.save(created);
        });

        if (!existingBySku.isPresent()) {
            ProductStockEntity stock = new ProductStockEntity();
            stock.setId(stableUuid("stock-" + seed));
            stock.setBranchId(branchId);
            stock.setProductId(product.getId());
            stock.setOnHandQty(initialQty);
            stock.setAvgUnitCost(avgCost);
            stock.setUpdatedAt(OffsetDateTime.now());
            productStockRepository.save(stock);
        } else if (productStockRepository.findByBranchIdAndProductId(branchId, product.getId()).isEmpty()) {
            ProductStockEntity stock = new ProductStockEntity();
            stock.setId(stableUuid("stock-" + seed));
            stock.setBranchId(branchId);
            stock.setProductId(product.getId());
            stock.setOnHandQty(initialQty);
            stock.setAvgUnitCost(avgCost);
            stock.setUpdatedAt(OffsetDateTime.now());
            productStockRepository.save(stock);
        }

        return product;
    }

    private void ensureBomForVaccination(UUID branchId, UUID vacunaProductId, UUID jeringaProductId) {
        ServiceEntity vacunacionService = serviceRepository.findFirstByBranchIdAndNameIgnoreCase(branchId, "Vacunacion")
            .orElse(null);
        if (vacunacionService == null) {
            return;
        }
        if (serviceBomItemRepository.existsByBranchIdAndServiceId(branchId, vacunacionService.getId())) {
            return;
        }

        ServiceBomItemEntity vacunaBom = new ServiceBomItemEntity();
        vacunaBom.setId(stableUuid("bom-vacunacion-vacuna"));
        vacunaBom.setBranchId(branchId);
        vacunaBom.setServiceId(vacunacionService.getId());
        vacunaBom.setProductId(vacunaProductId);
        vacunaBom.setQty(new BigDecimal("1.000"));

        ServiceBomItemEntity jeringaBom = new ServiceBomItemEntity();
        jeringaBom.setId(stableUuid("bom-vacunacion-jeringa"));
        jeringaBom.setBranchId(branchId);
        jeringaBom.setServiceId(vacunacionService.getId());
        jeringaBom.setProductId(jeringaProductId);
        jeringaBom.setQty(new BigDecimal("1.000"));

        serviceBomItemRepository.save(vacunaBom);
        serviceBomItemRepository.save(jeringaBom);
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

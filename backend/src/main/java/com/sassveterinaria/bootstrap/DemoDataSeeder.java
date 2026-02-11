package com.sassveterinaria.bootstrap;

import com.sassveterinaria.auth.domain.AppUserEntity;
import com.sassveterinaria.auth.domain.BranchEntity;
import com.sassveterinaria.auth.domain.UserBranchEntity;
import com.sassveterinaria.auth.domain.UserBranchId;
import com.sassveterinaria.auth.repo.AppUserRepository;
import com.sassveterinaria.auth.repo.BranchRepository;
import com.sassveterinaria.auth.repo.UserBranchRepository;
import com.sassveterinaria.appointment.domain.AppointmentEntity;
import com.sassveterinaria.appointment.domain.RoomEntity;
import com.sassveterinaria.appointment.domain.ServiceEntity;
import com.sassveterinaria.appointment.repo.AppointmentRepository;
import com.sassveterinaria.appointment.repo.RoomRepository;
import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.billing.domain.TaxConfigEntity;
import com.sassveterinaria.billing.repo.TaxConfigRepository;
import com.sassveterinaria.clinical.domain.VisitEntity;
import com.sassveterinaria.clinical.repo.VisitRepository;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.demo.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private final BranchRepository branchRepository;
    private final AppUserRepository appUserRepository;
    private final UserBranchRepository userBranchRepository;
    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;
    private final TaxConfigRepository taxConfigRepository;
    private final VisitRepository visitRepository;
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
        AppointmentRepository appointmentRepository,
        RoomRepository roomRepository,
        ServiceRepository serviceRepository,
        TaxConfigRepository taxConfigRepository,
        VisitRepository visitRepository,
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
        this.appointmentRepository = appointmentRepository;
        this.roomRepository = roomRepository;
        this.serviceRepository = serviceRepository;
        this.taxConfigRepository = taxConfigRepository;
        this.visitRepository = visitRepository;
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
        DemoUsers users = ensureDemoUsers(branch.getId());
        ServiceEntity consultaService = ensureDemoServices(branch.getId());
        RoomEntity room = ensureDemoRoom(branch.getId());
        DemoClientPet demoClientPet = ensureDemoClientAndPet(branch.getId());

        ensureDemoAppointmentsAndVisit(
            branch.getId(),
            room.getId(),
            consultaService.getId(),
            consultaService.getDurationMinutes(),
            demoClientPet.clientId(),
            demoClientPet.petId(),
            users.veterinarioId()
        );
        ensureInventorySeeds(branch.getId());
        ensureTaxConfig(branch.getId(), users.superadminId());
    }

    private BranchEntity ensureDemoBranch() {
        UUID branchId = stableUuid("branch-centro");
        BranchEntity branch = branchRepository.findById(branchId).orElseGet(() -> {
            BranchEntity created = new BranchEntity();
            created.setId(branchId);
            created.setCreatedAt(OffsetDateTime.now());
            return created;
        });
        branch.setCode("CENTRO");
        branch.setName("Sucursal Centro");
        branch.setActive(true);
        return branchRepository.save(branch);
    }

    private DemoUsers ensureDemoUsers(UUID branchId) {
        AppUserEntity superadmin = ensureUser(
            "superadmin",
            "Super Admin",
            "SUPERADMIN",
            "SuperAdmin123!",
            branchId,
            true
        );
        AppUserEntity admin = ensureUser(
            "admin",
            "Admin",
            "ADMIN",
            "Admin123!",
            branchId,
            true
        );
        AppUserEntity recepcion = ensureUser(
            "recepcion",
            "Recepcion",
            "RECEPCION",
            "Recepcion123!",
            branchId,
            true
        );
        AppUserEntity veterinario = ensureUser(
            "veterinario",
            "Veterinario",
            "VETERINARIO",
            "Veterinario123!",
            branchId,
            true
        );
        return new DemoUsers(superadmin.getId(), admin.getId(), recepcion.getId(), veterinario.getId());
    }

    private AppUserEntity ensureUser(
        String username,
        String fullName,
        String roleCode,
        String password,
        UUID branchId,
        boolean defaultBranch
    ) {
        AppUserEntity user = appUserRepository.findByEmail(username).orElseGet(() -> {
            AppUserEntity created = new AppUserEntity();
            created.setId(stableUuid("user-" + username));
            created.setCreatedAt(OffsetDateTime.now());
            return created;
        });
        user.setEmail(username);
        user.setFullName(fullName);
        user.setRoleCode(roleCode);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setActive(true);
        user.setLockedUntil(null);
        user.setTotpSecret(null);
        user.setTotpEnabled(false);
        user.setTotpVerifiedAt(null);
        AppUserEntity saved = appUserRepository.save(user);
        ensureUserBranch(saved.getId(), branchId, defaultBranch);
        return saved;
    }

    private void ensureUserBranch(UUID userId, UUID branchId, boolean isDefault) {
        UserBranchId userBranchId = new UserBranchId(userId, branchId);
        UserBranchEntity userBranch = userBranchRepository.findById(userBranchId).orElseGet(() -> {
            UserBranchEntity created = new UserBranchEntity();
            created.setId(userBranchId);
            return created;
        });
        userBranch.setDefault(isDefault);
        userBranchRepository.save(userBranch);
    }

    private RoomEntity ensureDemoRoom(UUID branchId) {
        String roomName = "Consultorio 1";
        Optional<RoomEntity> existing = roomRepository.findByBranchIdAndIsActiveTrueOrderByNameAsc(branchId)
            .stream()
            .filter(room -> roomName.equalsIgnoreCase(room.getName()))
            .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }

        RoomEntity room = new RoomEntity();
        room.setId(stableUuid("room-demo-01"));
        room.setBranchId(branchId);
        room.setName(roomName);
        room.setActive(true);
        room.setCreatedAt(OffsetDateTime.now());
        return roomRepository.save(room);
    }

    private DemoClientPet ensureDemoClientAndPet(UUID branchId) {
        UUID clientId = stableUuid("client-demo-1");
        ClientEntity client = clientRepository.findByIdAndBranchId(clientId, branchId).orElseGet(() -> {
            ClientEntity created = new ClientEntity();
            created.setId(clientId);
            created.setBranchId(branchId);
            created.setCreatedAt(OffsetDateTime.now());
            return created;
        });
        client.setFullName("Luis Demo");
        client.setIdentification("0102030405");
        client.setPhone("0990000001");
        client.setEmail("demo.cliente@example.com");
        client.setAddress("Av. Demo 123");
        client.setNotes("Cliente seed para smoke CRM.");
        ClientEntity savedClient = clientRepository.save(client);

        UUID petId = stableUuid("pet-demo-1");
        PetEntity pet = petRepository.findByIdAndBranchId(petId, branchId).orElseGet(() -> {
            PetEntity created = new PetEntity();
            created.setId(petId);
            created.setBranchId(branchId);
            created.setCreatedAt(OffsetDateTime.now());
            return created;
        });
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
        PetEntity savedPet = petRepository.save(pet);

        return new DemoClientPet(savedClient.getId(), savedPet.getId());
    }

    private ServiceEntity ensureDemoServices(UUID branchId) {
        ServiceEntity consulta = ensureService(
            branchId,
            "service-demo-consulta-general",
            "Consulta general",
            30,
            new BigDecimal("20.00")
        );
        ensureService(branchId, "service-demo-vacunacion", "Vacunacion", 20, new BigDecimal("15.00"));
        ensureService(branchId, "service-demo-control-post", "Control post-operatorio", 30, new BigDecimal("18.00"));
        return consulta;
    }

    private void ensureDemoAppointmentsAndVisit(
        UUID branchId,
        UUID roomId,
        UUID serviceId,
        int serviceDurationMinutes,
        UUID clientId,
        UUID petId,
        UUID veterinarianId
    ) {
        OffsetDateTime closedStart = OffsetDateTime.now().minusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime upcomingStart = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        AppointmentEntity closedAppointment = ensureAppointment(
            "appointment-demo-1",
            branchId,
            roomId,
            serviceId,
            serviceDurationMinutes,
            clientId,
            petId,
            veterinarianId,
            closedStart,
            "CLOSED",
            "Control de seguimiento demo"
        );

        ensureAppointment(
            "appointment-demo-2",
            branchId,
            roomId,
            serviceId,
            serviceDurationMinutes,
            clientId,
            petId,
            veterinarianId,
            upcomingStart,
            "RESERVED",
            "Cita demo pendiente"
        );

        ensureVisitExample(branchId, petId, serviceId, closedAppointment.getId(), veterinarianId, closedStart.plusMinutes(5));
    }

    private AppointmentEntity ensureAppointment(
        String seed,
        UUID branchId,
        UUID roomId,
        UUID serviceId,
        int durationMinutes,
        UUID clientId,
        UUID petId,
        UUID veterinarianId,
        OffsetDateTime startsAt,
        String status,
        String notes
    ) {
        UUID appointmentId = stableUuid(seed);
        return appointmentRepository.findById(appointmentId).orElseGet(() -> {
            AppointmentEntity appointment = new AppointmentEntity();
            appointment.setId(appointmentId);
            appointment.setBranchId(branchId);
            appointment.setStartsAt(startsAt);
            appointment.setEndsAt(startsAt.plusMinutes(durationMinutes));
            appointment.setStatus(status);
            appointment.setReason("Seed demo");
            appointment.setNotes(notes);
            appointment.setRoomId(roomId);
            appointment.setServiceId(serviceId);
            appointment.setClientId(clientId);
            appointment.setPetId(petId);
            appointment.setVeterinarianId(veterinarianId);
            appointment.setCreatedAt(OffsetDateTime.now());
            appointment.setCheckedInAt("CLOSED".equals(status) ? startsAt.plusMinutes(3) : null);
            appointment.setOverbook(false);
            appointment.setOverbookReason(null);
            return appointmentRepository.save(appointment);
        });
    }

    private void ensureVisitExample(
        UUID branchId,
        UUID petId,
        UUID serviceId,
        UUID appointmentId,
        UUID veterinarianId,
        OffsetDateTime createdAt
    ) {
        UUID visitId = stableUuid("visit-demo-1");
        if (visitRepository.findById(visitId).isPresent()) {
            return;
        }

        VisitEntity visit = new VisitEntity();
        visit.setId(visitId);
        visit.setBranchId(branchId);
        visit.setPetId(petId);
        visit.setServiceId(serviceId);
        visit.setAppointmentId(appointmentId);
        visit.setStatus("CLOSED");
        visit.setSReason("Control general de rutina.");
        visit.setSAnamnesis("Paciente activo, apetito normal.");
        visit.setOWeightKg(new BigDecimal("12.40"));
        visit.setOTemperatureC(new BigDecimal("38.4"));
        visit.setOFindings("Sin hallazgos patologicos.");
        visit.setADiagnosis("Control preventivo.");
        visit.setASeverity("LOW");
        visit.setPTreatment("No requiere medicacion.");
        visit.setPInstructions("Control anual y esquema de vacunas al dia.");
        visit.setPFollowupAt(LocalDate.now().plusMonths(12));
        visit.setCreatedBy(veterinarianId);
        visit.setCreatedAt(createdAt);
        visit.setUpdatedAt(createdAt.plusMinutes(20));
        visitRepository.save(visit);
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

    private void ensureTaxConfig(UUID branchId, UUID updatedBy) {
        if (taxConfigRepository.findByBranchId(branchId).isPresent()) {
            return;
        }
        TaxConfigEntity config = new TaxConfigEntity();
        config.setId(stableUuid("tax-config-" + branchId));
        config.setBranchId(branchId);
        config.setTaxRate(new BigDecimal("0.1500"));
        config.setUpdatedBy(updatedBy);
        config.setUpdatedAt(OffsetDateTime.now());
        taxConfigRepository.save(config);
    }

    private ServiceEntity ensureService(UUID branchId, String seed, String name, int durationMinutes, BigDecimal priceBase) {
        return serviceRepository.findFirstByBranchIdAndNameIgnoreCase(branchId, name).orElseGet(() -> {
            ServiceEntity service = new ServiceEntity();
            service.setId(stableUuid(seed));
            service.setBranchId(branchId);
            service.setName(name);
            service.setDurationMinutes(durationMinutes);
            service.setPriceBase(priceBase);
            service.setActive(true);
            service.setCreatedAt(OffsetDateTime.now());
            return serviceRepository.save(service);
        });
    }

    private UUID stableUuid(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    private record DemoUsers(UUID superadminId, UUID adminId, UUID recepcionId, UUID veterinarioId) {
    }

    private record DemoClientPet(UUID clientId, UUID petId) {
    }
}

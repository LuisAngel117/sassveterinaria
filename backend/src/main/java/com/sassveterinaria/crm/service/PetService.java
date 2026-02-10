package com.sassveterinaria.crm.service;

import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.crm.domain.ClientEntity;
import com.sassveterinaria.crm.domain.PetEntity;
import com.sassveterinaria.crm.dto.PetCreateRequest;
import com.sassveterinaria.crm.dto.PetPatchRequest;
import com.sassveterinaria.crm.dto.PetResponse;
import com.sassveterinaria.crm.repo.ClientRepository;
import com.sassveterinaria.crm.repo.PetRepository;
import com.sassveterinaria.security.AuthPrincipal;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final ClientRepository clientRepository;

    public PetService(PetRepository petRepository, ClientRepository clientRepository) {
        this.petRepository = petRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public PetResponse create(AuthPrincipal principal, UUID clientId, PetCreateRequest request) {
        ClientEntity client = requireClient(principal, clientId);
        String internalCode = requiredValue(request.internalCode(), "internalCode es requerido.", "PET_INTERNAL_CODE_REQUIRED", 30);
        ensureInternalCodeUnique(principal.getBranchId(), internalCode, null);

        PetEntity entity = new PetEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setClientId(client.getId());
        entity.setInternalCode(internalCode);
        entity.setName(requiredValue(request.name(), "name es requerido.", "PET_NAME_REQUIRED", 120));
        entity.setSpecies(requiredValue(request.species(), "species es requerido.", "PET_SPECIES_REQUIRED", 80));
        entity.setBreed(normalizeOptional(request.breed(), 120));
        entity.setSex(normalizeOptional(request.sex(), 20));
        entity.setBirthDate(request.birthDate());
        entity.setWeightKg(normalizeWeight(request.weightKg()));
        entity.setNeutered(request.neutered());
        entity.setAlerts(normalizeText(request.alerts()));
        entity.setHistory(normalizeText(request.history()));
        entity.setCreatedAt(OffsetDateTime.now());
        return toResponse(petRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<PetResponse> listByClient(AuthPrincipal principal, UUID clientId) {
        requireClient(principal, clientId);
        return petRepository
            .findByBranchIdAndClientIdOrderByNameAsc(principal.getBranchId(), clientId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PetResponse get(AuthPrincipal principal, UUID petId) {
        return toResponse(requirePet(principal, petId));
    }

    @Transactional
    public PetResponse update(AuthPrincipal principal, UUID petId, PetPatchRequest request) {
        PetEntity entity = requirePet(principal, petId);

        if (request.internalCode() != null) {
            String internalCode = requiredValue(request.internalCode(), "internalCode no puede ser vacio.", "PET_INTERNAL_CODE_REQUIRED", 30);
            ensureInternalCodeUnique(principal.getBranchId(), internalCode, entity.getId());
            entity.setInternalCode(internalCode);
        }
        if (request.name() != null) {
            entity.setName(requiredValue(request.name(), "name no puede ser vacio.", "PET_NAME_REQUIRED", 120));
        }
        if (request.species() != null) {
            entity.setSpecies(requiredValue(request.species(), "species no puede ser vacio.", "PET_SPECIES_REQUIRED", 80));
        }
        if (request.breed() != null) {
            entity.setBreed(normalizeOptional(request.breed(), 120));
        }
        if (request.sex() != null) {
            entity.setSex(normalizeOptional(request.sex(), 20));
        }
        if (request.birthDate() != null) {
            entity.setBirthDate(request.birthDate());
        }
        if (request.weightKg() != null) {
            entity.setWeightKg(normalizeWeight(request.weightKg()));
        }
        if (request.neutered() != null) {
            entity.setNeutered(request.neutered());
        }
        if (request.alerts() != null) {
            entity.setAlerts(normalizeText(request.alerts()));
        }
        if (request.history() != null) {
            entity.setHistory(normalizeText(request.history()));
        }

        return toResponse(petRepository.save(entity));
    }

    private ClientEntity requireClient(AuthPrincipal principal, UUID clientId) {
        return clientRepository.findByIdAndBranchId(clientId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/client-not-found",
                "Client not found",
                "No se encontro el cliente.",
                "CLIENT_NOT_FOUND"
            ));
    }

    private PetEntity requirePet(AuthPrincipal principal, UUID petId) {
        return petRepository.findByIdAndBranchId(petId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/pet-not-found",
                "Pet not found",
                "No se encontro la mascota.",
                "PET_NOT_FOUND"
            ));
    }

    private void ensureInternalCodeUnique(UUID branchId, String internalCode, UUID currentPetId) {
        boolean exists;
        if (currentPetId == null) {
            exists = petRepository.existsByBranchIdAndInternalCodeIgnoreCase(branchId, internalCode);
        } else {
            exists = petRepository.existsByBranchIdAndInternalCodeIgnoreCaseAndIdNot(branchId, internalCode, currentPetId);
        }
        if (exists) {
            throw new ApiProblemException(
                HttpStatus.CONFLICT,
                "https://sassveterinaria.local/errors/pet-internal-code-conflict",
                "Pet internal code conflict",
                "Ya existe una mascota con ese internalCode en la sucursal.",
                "PET_INTERNAL_CODE_CONFLICT"
            );
        }
    }

    private String requiredValue(String value, String detail, String errorCode, int maxLength) {
        String normalized = normalizeOptional(value, maxLength);
        if (normalized == null) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/validation",
                "Validation error",
                detail,
                errorCode
            );
        }
        return normalized;
    }

    private String normalizeOptional(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/validation",
                "Validation error",
                "El valor supera el maximo permitido.",
                "VALIDATION_ERROR"
            );
        }
        return normalized;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private BigDecimal normalizeWeight(BigDecimal weightKg) {
        if (weightKg == null) {
            return null;
        }
        if (weightKg.compareTo(BigDecimal.ZERO) <= 0 || weightKg.compareTo(new BigDecimal("9999.99")) > 0) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/validation",
                "Validation error",
                "weightKg debe ser mayor a 0 y menor o igual a 9999.99.",
                "PET_WEIGHT_INVALID"
            );
        }
        return weightKg;
    }

    private PetResponse toResponse(PetEntity entity) {
        return new PetResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getClientId(),
            entity.getInternalCode(),
            entity.getName(),
            entity.getSpecies(),
            entity.getBreed(),
            entity.getSex(),
            entity.getBirthDate(),
            entity.getWeightKg(),
            entity.getNeutered(),
            entity.getAlerts(),
            entity.getHistory(),
            entity.getCreatedAt()
        );
    }
}

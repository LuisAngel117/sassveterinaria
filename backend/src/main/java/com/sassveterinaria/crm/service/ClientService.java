package com.sassveterinaria.crm.service;

import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.crm.domain.ClientEntity;
import com.sassveterinaria.crm.dto.ClientCreateRequest;
import com.sassveterinaria.crm.dto.ClientPatchRequest;
import com.sassveterinaria.crm.dto.ClientResponse;
import com.sassveterinaria.crm.repo.ClientRepository;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public ClientResponse create(AuthPrincipal principal, ClientCreateRequest request) {
        ClientEntity entity = new ClientEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setFullName(requiredValue(request.fullName(), "fullName es requerido.", "CLIENT_FULL_NAME_REQUIRED", 160));
        entity.setIdentification(normalizeIdentification(request.identification()));
        entity.setPhone(normalizeOptional(request.phone(), 30));
        entity.setEmail(normalizeOptional(request.email(), 160));
        entity.setAddress(normalizeOptional(request.address(), 255));
        entity.setNotes(normalizeText(request.notes()));
        entity.setCreatedAt(OffsetDateTime.now());
        return toResponse(clientRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> search(AuthPrincipal principal, String q, Pageable pageable) {
        return clientRepository
            .search(principal.getBranchId(), normalizeSearch(q), pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ClientResponse get(AuthPrincipal principal, UUID clientId) {
        return toResponse(requireClient(principal, clientId));
    }

    @Transactional
    public ClientResponse update(AuthPrincipal principal, UUID clientId, ClientPatchRequest request) {
        ClientEntity entity = requireClient(principal, clientId);

        if (request.fullName() != null) {
            entity.setFullName(requiredValue(request.fullName(), "fullName no puede ser vacio.", "CLIENT_FULL_NAME_REQUIRED", 160));
        }
        if (request.identification() != null) {
            entity.setIdentification(normalizeIdentification(request.identification()));
        }
        if (request.phone() != null) {
            entity.setPhone(normalizeOptional(request.phone(), 30));
        }
        if (request.email() != null) {
            entity.setEmail(normalizeOptional(request.email(), 160));
        }
        if (request.address() != null) {
            entity.setAddress(normalizeOptional(request.address(), 255));
        }
        if (request.notes() != null) {
            entity.setNotes(normalizeText(request.notes()));
        }

        return toResponse(clientRepository.save(entity));
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

    private String normalizeSearch(String q) {
        if (q == null) {
            return null;
        }
        String normalized = q.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeIdentification(String rawIdentification) {
        String normalized = normalizeOptional(rawIdentification, 30);
        if (normalized == null) {
            return null;
        }
        if (!normalized.matches("^(\\d{10}|\\d{13})$")) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/client-identification-invalid",
                "Invalid identification",
                "identification debe tener 10 o 13 digitos.",
                "CLIENT_IDENTIFICATION_INVALID"
            );
        }
        return normalized;
    }

    private ClientResponse toResponse(ClientEntity entity) {
        return new ClientResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getFullName(),
            entity.getIdentification(),
            entity.getPhone(),
            entity.getEmail(),
            entity.getAddress(),
            entity.getNotes(),
            entity.getCreatedAt()
        );
    }
}

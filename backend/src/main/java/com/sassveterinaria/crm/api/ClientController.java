package com.sassveterinaria.crm.api;

import com.sassveterinaria.crm.dto.ClientCreateRequest;
import com.sassveterinaria.crm.dto.ClientPatchRequest;
import com.sassveterinaria.crm.dto.ClientResponse;
import com.sassveterinaria.crm.dto.PetCreateRequest;
import com.sassveterinaria.crm.dto.PetResponse;
import com.sassveterinaria.crm.service.ClientService;
import com.sassveterinaria.crm.service.PetService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;
    private final PetService petService;

    public ClientController(ClientService clientService, PetService petService) {
        this.clientService = clientService;
        this.petService = petService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENT_CREATE')")
    public ResponseEntity<ClientResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody ClientCreateRequest request
    ) {
        return ResponseEntity.ok(clientService.create(principal, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENT_READ')")
    public ResponseEntity<Page<ClientResponse>> list(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam(value = "q", required = false) String q,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(clientService.search(principal, q, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_READ')")
    public ResponseEntity<ClientResponse> get(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID clientId
    ) {
        return ResponseEntity.ok(clientService.get(principal, clientId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_UPDATE')")
    public ResponseEntity<ClientResponse> update(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID clientId,
        @Valid @RequestBody ClientPatchRequest request
    ) {
        return ResponseEntity.ok(clientService.update(principal, clientId, request));
    }

    @PostMapping("/{clientId}/pets")
    @PreAuthorize("hasAuthority('PET_CREATE')")
    public ResponseEntity<PetResponse> createPet(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("clientId") UUID clientId,
        @Valid @RequestBody PetCreateRequest request
    ) {
        return ResponseEntity.ok(petService.create(principal, clientId, request));
    }

    @GetMapping("/{clientId}/pets")
    @PreAuthorize("hasAuthority('PET_READ')")
    public ResponseEntity<List<PetResponse>> listPets(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("clientId") UUID clientId
    ) {
        return ResponseEntity.ok(petService.listByClient(principal, clientId));
    }
}

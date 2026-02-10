package com.sassveterinaria.crm.api;

import com.sassveterinaria.crm.dto.PetPatchRequest;
import com.sassveterinaria.crm.dto.PetResponse;
import com.sassveterinaria.crm.service.PetService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PET_READ')")
    public ResponseEntity<PetResponse> get(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID petId
    ) {
        return ResponseEntity.ok(petService.get(principal, petId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PET_UPDATE')")
    public ResponseEntity<PetResponse> update(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID petId,
        @Valid @RequestBody PetPatchRequest request
    ) {
        return ResponseEntity.ok(petService.update(principal, petId, request));
    }
}

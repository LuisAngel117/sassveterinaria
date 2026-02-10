package com.sassveterinaria.auth.api;

import com.sassveterinaria.auth.dto.AuthResponse;
import com.sassveterinaria.auth.dto.LoginRequest;
import com.sassveterinaria.auth.dto.LogoutRequest;
import com.sassveterinaria.auth.dto.MeResponse;
import com.sassveterinaria.auth.dto.RefreshRequest;
import com.sassveterinaria.auth.service.AuthService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(new MeResponse(
            principal.getUserId(),
            principal.getUsername(),
            principal.getFullName(),
            principal.getRoleCode(),
            principal.getBranchId(),
            principal.getPermissions()
        ));
    }
}

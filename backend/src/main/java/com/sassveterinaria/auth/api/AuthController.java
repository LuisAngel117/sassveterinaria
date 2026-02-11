package com.sassveterinaria.auth.api;

import com.sassveterinaria.auth.dto.AuthResponse;
import com.sassveterinaria.auth.dto.Login2faRequest;
import com.sassveterinaria.auth.dto.LoginRequest;
import com.sassveterinaria.auth.dto.LoginResponse;
import com.sassveterinaria.auth.dto.LogoutRequest;
import com.sassveterinaria.auth.dto.MeResponse;
import com.sassveterinaria.auth.dto.RefreshRequest;
import com.sassveterinaria.auth.dto.TwoFactorEnableRequest;
import com.sassveterinaria.auth.dto.TwoFactorSetupResponse;
import com.sassveterinaria.auth.service.AuthService;
import com.sassveterinaria.security.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/login/2fa")
    public ResponseEntity<AuthResponse> login2fa(@Valid @RequestBody Login2faRequest request) {
        return ResponseEntity.ok(authService.loginWithTwoFactor(request));
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

    @PostMapping("/auth/2fa/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TwoFactorSetupResponse> setupTwoFactor(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(authService.setupTwoFactor(principal));
    }

    @PostMapping("/auth/2fa/enable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> enableTwoFactor(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody TwoFactorEnableRequest request
    ) {
        authService.enableTwoFactor(principal, request);
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

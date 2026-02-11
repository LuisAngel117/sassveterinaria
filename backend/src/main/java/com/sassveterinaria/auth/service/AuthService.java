package com.sassveterinaria.auth.service;

import com.sassveterinaria.auth.domain.AppUserEntity;
import com.sassveterinaria.auth.domain.AuthLoginAttemptEntity;
import com.sassveterinaria.auth.domain.AuthTwoFactorChallengeEntity;
import com.sassveterinaria.auth.domain.BranchEntity;
import com.sassveterinaria.auth.domain.RefreshTokenEntity;
import com.sassveterinaria.auth.domain.UserBranchEntity;
import com.sassveterinaria.auth.dto.AuthResponse;
import com.sassveterinaria.auth.dto.Login2faRequest;
import com.sassveterinaria.auth.dto.LoginRequest;
import com.sassveterinaria.auth.dto.LoginResponse;
import com.sassveterinaria.auth.dto.RefreshRequest;
import com.sassveterinaria.auth.dto.TwoFactorEnableRequest;
import com.sassveterinaria.auth.dto.TwoFactorSetupResponse;
import com.sassveterinaria.auth.repo.AppUserRepository;
import com.sassveterinaria.auth.repo.AuthLoginAttemptRepository;
import com.sassveterinaria.auth.repo.AuthTwoFactorChallengeRepository;
import com.sassveterinaria.auth.repo.BranchRepository;
import com.sassveterinaria.auth.repo.RefreshTokenRepository;
import com.sassveterinaria.auth.repo.UserBranchRepository;
import com.sassveterinaria.audit.service.AuditService;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.JwtProperties;
import com.sassveterinaria.security.RateLimitService;
import com.sassveterinaria.security.SecurityHardeningProperties;
import com.sassveterinaria.security.JwtService;
import com.sassveterinaria.security.PermissionMatrix;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final UserBranchRepository userBranchRepository;
    private final BranchRepository branchRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthLoginAttemptRepository authLoginAttemptRepository;
    private final AuthTwoFactorChallengeRepository authTwoFactorChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuditService auditService;
    private final TotpService totpService;
    private final TotpCryptoService totpCryptoService;
    private final SecurityHardeningProperties hardeningProperties;
    private final RateLimitService rateLimitService;

    public AuthService(
        AppUserRepository appUserRepository,
        UserBranchRepository userBranchRepository,
        BranchRepository branchRepository,
        RefreshTokenRepository refreshTokenRepository,
        AuthLoginAttemptRepository authLoginAttemptRepository,
        AuthTwoFactorChallengeRepository authTwoFactorChallengeRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        JwtProperties jwtProperties,
        AuditService auditService,
        TotpService totpService,
        TotpCryptoService totpCryptoService,
        SecurityHardeningProperties hardeningProperties,
        RateLimitService rateLimitService
    ) {
        this.appUserRepository = appUserRepository;
        this.userBranchRepository = userBranchRepository;
        this.branchRepository = branchRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authLoginAttemptRepository = authLoginAttemptRepository;
        this.authTwoFactorChallengeRepository = authTwoFactorChallengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.auditService = auditService;
        this.totpService = totpService;
        this.totpCryptoService = totpCryptoService;
        this.hardeningProperties = hardeningProperties;
        this.rateLimitService = rateLimitService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        rateLimitService.checkLogin(request.username());

        AppUserEntity user = appUserRepository.findByEmail(request.username())
            .orElseThrow(this::invalidCredentials);

        if (!user.isActive()) {
            throw new ApiProblemException(
                HttpStatus.FORBIDDEN,
                "https://sassveterinaria.local/errors/user-inactive",
                "User inactive",
                "El usuario esta inactivo.",
                "USER_INACTIVE"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            recordFailedAttempt(user, request.username(), "USER_LOCKED");
            throw new ApiProblemException(
                HttpStatus.LOCKED,
                "https://sassveterinaria.local/errors/user-locked",
                "User locked",
                "El usuario esta bloqueado temporalmente.",
                "USER_LOCKED"
            );
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user, request.username());
            throw invalidCredentials();
        }
        recordSuccessfulAttempt(user, request.username());
        clearLockIfExpired(user);

        BranchEntity branch = resolveDefaultBranch(user.getId());
        List<String> permissions = PermissionMatrix.permissionsForRole(user.getRoleCode());

        boolean twoFaRequired = isTwoFaRequired(user.getRoleCode());
        boolean totpEnabled = user.isTotpEnabled();
        if (twoFaRequired && totpEnabled) {
            ChallengeBundle challenge = createTwoFactorChallenge(user.getId(), branch.getId());
            auditService.recordAuthEvent(
                user.getId(),
                user.getEmail(),
                branch.getId(),
                "AUTH_LOGIN_CHALLENGE",
                "auth_session",
                user.getId(),
                Map.of("challengeExpiresAt", challenge.expiresAt())
            );
            return new LoginResponse(
                null,
                null,
                null,
                null,
                null,
                true,
                challenge.token(),
                hardeningProperties.getTwoFa().getChallengeSeconds(),
                false,
                "2FA challenge requerido."
            );
        }

        if (twoFaRequired && !totpEnabled && !hardeningProperties.getTwoFa().isAllowLoginWithoutEnrollment()) {
            throw new ApiProblemException(
                HttpStatus.FORBIDDEN,
                "https://sassveterinaria.local/errors/totp-setup-required",
                "2FA setup required",
                "Debe activar 2FA TOTP para iniciar sesion.",
                "TOTP_SETUP_REQUIRED"
            );
        }

        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRoleCode(),
            branch.getId(),
            permissions
        );

        String refreshToken = createAndStoreRefreshToken(user.getId(), null);
        auditService.recordAuthEvent(
            user.getId(),
            user.getEmail(),
            branch.getId(),
            "AUTH_LOGIN",
            "auth_session",
            user.getId(),
            Map.of("roleCode", user.getRoleCode(), "branchId", branch.getId())
        );

        return new LoginResponse(
            accessToken,
            refreshToken,
            jwtProperties.accessTokenSeconds(),
            new AuthResponse.UserPayload(user.getId(), user.getEmail(), user.getFullName(), user.getRoleCode()),
            new AuthResponse.BranchPayload(branch.getId(), branch.getCode(), branch.getName()),
            false,
            null,
            null,
            twoFaRequired && !totpEnabled,
            twoFaRequired && !totpEnabled ? "2FA recomendado: active TOTP desde setup." : null
        );
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        rateLimitService.checkRefresh(request.refreshToken());

        String refreshHash = hashToken(request.refreshToken());
        OffsetDateTime now = OffsetDateTime.now();

        RefreshTokenEntity storedToken = refreshTokenRepository.findActiveByTokenHash(refreshHash, now)
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.UNAUTHORIZED,
                "https://sassveterinaria.local/errors/invalid-refresh-token",
                "Invalid refresh token",
                "El refresh token no es valido o expiro.",
                "INVALID_REFRESH_TOKEN"
            ));

        AppUserEntity user = appUserRepository.findById(storedToken.getUserId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.UNAUTHORIZED,
                "https://sassveterinaria.local/errors/user-not-found",
                "User not found",
                "No se encontro el usuario del token.",
                "USER_NOT_FOUND"
            ));

        BranchEntity branch = resolveDefaultBranch(user.getId());
        List<String> permissions = PermissionMatrix.permissionsForRole(user.getRoleCode());

        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRoleCode(),
            branch.getId(),
            permissions
        );

        storedToken.setRevokedAt(now);
        refreshTokenRepository.save(storedToken);

        String newRefreshToken = createAndStoreRefreshToken(user.getId(), storedToken.getId());
        auditService.recordAuthEvent(
            user.getId(),
            user.getEmail(),
            branch.getId(),
            "AUTH_REFRESH",
            "auth_session",
            user.getId(),
            Map.of("branchId", branch.getId(), "refreshTokenId", storedToken.getId())
        );

        return buildAuthResponse(user, branch, accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        String refreshHash = hashToken(refreshToken);
        OffsetDateTime now = OffsetDateTime.now();
        refreshTokenRepository.findActiveByTokenHash(refreshHash, now).ifPresent(token -> {
            token.setRevokedAt(now);
            refreshTokenRepository.save(token);

            appUserRepository.findById(token.getUserId()).ifPresent(user -> auditService.recordAuthEvent(
                user.getId(),
                user.getEmail(),
                resolveBranchIdNullable(user.getId()),
                "AUTH_LOGOUT",
                "auth_session",
                user.getId(),
                Map.of("refreshTokenId", token.getId())
            ));
        });
    }

    @Transactional
    public TwoFactorSetupResponse setupTwoFactor(com.sassveterinaria.security.AuthPrincipal principal) {
        requireAdminOrSuperadmin(principal.getRoleCode());
        AppUserEntity user = appUserRepository.findById(principal.getUserId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.UNAUTHORIZED,
                "https://sassveterinaria.local/errors/user-not-found",
                "User not found",
                "No se encontro el usuario autenticado.",
                "USER_NOT_FOUND"
            ));

        String secret = totpService.generateSecret();
        user.setTotpSecret(totpCryptoService.encrypt(secret));
        user.setTotpEnabled(false);
        user.setTotpVerifiedAt(null);
        appUserRepository.save(user);

        String issuer = hardeningProperties.getTwoFa().getIssuer();
        String otpauthUri = totpService.buildOtpAuthUri(issuer, user.getEmail(), secret);
        auditService.recordAuthEvent(
            user.getId(),
            user.getEmail(),
            principal.getBranchId(),
            "AUTH_2FA_SETUP",
            "app_user",
            user.getId(),
            Map.of("totpEnabled", false)
        );
        return new TwoFactorSetupResponse(secret, otpauthUri, issuer, user.getEmail());
    }

    @Transactional
    public void enableTwoFactor(com.sassveterinaria.security.AuthPrincipal principal, TwoFactorEnableRequest request) {
        requireAdminOrSuperadmin(principal.getRoleCode());
        AppUserEntity user = appUserRepository.findById(principal.getUserId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.UNAUTHORIZED,
                "https://sassveterinaria.local/errors/user-not-found",
                "User not found",
                "No se encontro el usuario autenticado.",
                "USER_NOT_FOUND"
            ));

        String encryptedSecret = user.getTotpSecret();
        if (encryptedSecret == null || encryptedSecret.isBlank()) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/totp-not-setup",
                "2FA not setup",
                "Debe ejecutar setup de 2FA antes de habilitar.",
                "TOTP_NOT_SETUP"
            );
        }

        String secret = totpCryptoService.decrypt(encryptedSecret);
        if (!totpService.verifyCode(secret, request.code())) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/totp-invalid-code",
                "Invalid TOTP code",
                "Codigo TOTP invalido.",
                "TOTP_INVALID_CODE"
            );
        }

        user.setTotpEnabled(true);
        user.setTotpVerifiedAt(OffsetDateTime.now());
        appUserRepository.save(user);

        auditService.recordAuthEvent(
            user.getId(),
            user.getEmail(),
            principal.getBranchId(),
            "AUTH_2FA_ENABLE",
            "app_user",
            user.getId(),
            Map.of("totpEnabled", true)
        );
    }

    @Transactional
    public AuthResponse loginWithTwoFactor(Login2faRequest request) {
        String challengeHash = hashToken(request.challengeToken());
        OffsetDateTime now = OffsetDateTime.now();
        AuthTwoFactorChallengeEntity challenge = authTwoFactorChallengeRepository
            .findActiveByChallengeHash(challengeHash, now)
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.UNAUTHORIZED,
                "https://sassveterinaria.local/errors/totp-challenge-invalid",
                "Invalid 2FA challenge",
                "El challenge de 2FA es invalido o expiro.",
                "TOTP_CHALLENGE_INVALID"
            ));

        AppUserEntity user = appUserRepository.findById(challenge.getUserId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.UNAUTHORIZED,
                "https://sassveterinaria.local/errors/user-not-found",
                "User not found",
                "No se encontro el usuario del challenge.",
                "USER_NOT_FOUND"
            ));
        if (!user.isTotpEnabled() || user.getTotpSecret() == null) {
            throw new ApiProblemException(
                HttpStatus.UNAUTHORIZED,
                "https://sassveterinaria.local/errors/totp-not-enabled",
                "2FA not enabled",
                "El usuario no tiene 2FA habilitado.",
                "TOTP_NOT_ENABLED"
            );
        }

        String secret = totpCryptoService.decrypt(user.getTotpSecret());
        if (!totpService.verifyCode(secret, request.code())) {
            throw new ApiProblemException(
                HttpStatus.UNAUTHORIZED,
                "https://sassveterinaria.local/errors/totp-invalid-code",
                "Invalid TOTP code",
                "Codigo TOTP invalido.",
                "TOTP_INVALID_CODE"
            );
        }

        BranchEntity branch = resolveDefaultBranch(user.getId());
        List<String> permissions = PermissionMatrix.permissionsForRole(user.getRoleCode());

        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRoleCode(),
            branch.getId(),
            permissions
        );
        String refreshToken = createAndStoreRefreshToken(user.getId(), null);
        challenge.setConsumedAt(OffsetDateTime.now());
        authTwoFactorChallengeRepository.save(challenge);

        auditService.recordAuthEvent(
            user.getId(),
            user.getEmail(),
            branch.getId(),
            "AUTH_LOGIN",
            "auth_session",
            user.getId(),
            Map.of("twoFactor", true, "branchId", branch.getId())
        );

        return buildAuthResponse(user, branch, accessToken, refreshToken);
    }

    private AuthResponse buildAuthResponse(AppUserEntity user, BranchEntity branch, String accessToken, String refreshToken) {
        return new AuthResponse(
            accessToken,
            refreshToken,
            jwtProperties.accessTokenSeconds(),
            new AuthResponse.UserPayload(user.getId(), user.getEmail(), user.getFullName(), user.getRoleCode()),
            new AuthResponse.BranchPayload(branch.getId(), branch.getCode(), branch.getName())
        );
    }

    private BranchEntity resolveDefaultBranch(UUID userId) {
        UserBranchEntity userBranch = userBranchRepository.findFirstByIdUserIdAndIsDefaultTrue(userId)
            .or(() -> userBranchRepository.findByIdUserId(userId).stream().findFirst())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.FORBIDDEN,
                "https://sassveterinaria.local/errors/branch-not-assigned",
                "Branch not assigned",
                "El usuario no tiene sucursal asignada.",
                "BRANCH_NOT_ASSIGNED"
            ));

        UUID branchId = userBranch.getId().getBranchId();
        return branchRepository.findById(branchId)
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.FORBIDDEN,
                "https://sassveterinaria.local/errors/branch-not-found",
                "Branch not found",
                "No se encontro la sucursal seleccionada.",
                "BRANCH_NOT_FOUND"
            ));
    }

    private UUID resolveBranchIdNullable(UUID userId) {
        return userBranchRepository.findFirstByIdUserIdAndIsDefaultTrue(userId)
            .or(() -> userBranchRepository.findByIdUserId(userId).stream().findFirst())
            .map(userBranch -> userBranch.getId().getBranchId())
            .orElse(null);
    }

    private String createAndStoreRefreshToken(UUID userId, UUID replacedTokenId) {
        OffsetDateTime now = OffsetDateTime.now();
        String plainToken = UUID.randomUUID().toString() + UUID.randomUUID();

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setId(UUID.randomUUID());
        refreshTokenEntity.setUserId(userId);
        refreshTokenEntity.setTokenHash(hashToken(plainToken));
        refreshTokenEntity.setIssuedAt(now);
        refreshTokenEntity.setExpiresAt(now.plusSeconds(jwtProperties.refreshTokenSeconds()));
        refreshTokenEntity.setRevokedAt(null);
        refreshTokenEntity.setReplacedBy(replacedTokenId);
        refreshTokenRepository.save(refreshTokenEntity);

        return plainToken;
    }

    private ChallengeBundle createTwoFactorChallenge(UUID userId, UUID branchId) {
        authTwoFactorChallengeRepository.deleteByExpiresAtBefore(OffsetDateTime.now().minusMinutes(1));

        String plainChallengeToken = UUID.randomUUID().toString() + UUID.randomUUID();
        AuthTwoFactorChallengeEntity challenge = new AuthTwoFactorChallengeEntity();
        challenge.setId(UUID.randomUUID());
        challenge.setUserId(userId);
        challenge.setBranchId(branchId);
        challenge.setChallengeHash(hashToken(plainChallengeToken));
        challenge.setCreatedAt(OffsetDateTime.now());
        challenge.setExpiresAt(OffsetDateTime.now().plusSeconds(hardeningProperties.getTwoFa().getChallengeSeconds()));
        challenge.setConsumedAt(null);
        authTwoFactorChallengeRepository.save(challenge);
        return new ChallengeBundle(plainChallengeToken, challenge.getExpiresAt());
    }

    private void recordFailedAttempt(AppUserEntity user, String username, String reason) {
        String normalizedUsername = username == null ? "-" : username.trim();
        AuthLoginAttemptEntity attempt = new AuthLoginAttemptEntity();
        attempt.setId(UUID.randomUUID());
        attempt.setUserId(user.getId());
        attempt.setUsername(normalizedUsername);
        attempt.setIp(currentIp());
        attempt.setSuccessful(false);
        attempt.setCreatedAt(OffsetDateTime.now());
        authLoginAttemptRepository.save(attempt);
        auditService.recordAuthEvent(
            user.getId(),
            user.getEmail(),
            resolveBranchIdNullable(user.getId()),
            "AUTH_LOGIN_FAILED",
            "auth_session",
            user.getId(),
            Map.of("reason", reason, "ip", currentIp())
        );
    }

    private void handleFailedLogin(AppUserEntity user, String username) {
        recordFailedAttempt(user, username, "INVALID_CREDENTIALS");

        OffsetDateTime windowStart = OffsetDateTime.now().minusMinutes(hardeningProperties.getLockout().getWindowMinutes());
        long failedCount = authLoginAttemptRepository.countByUserIdAndSuccessfulFalseAndCreatedAtGreaterThanEqual(
            user.getId(),
            windowStart
        );
        if (failedCount >= hardeningProperties.getLockout().getMaxAttempts()) {
            OffsetDateTime lockedUntil = OffsetDateTime.now().plusMinutes(hardeningProperties.getLockout().getDurationMinutes());
            user.setLockedUntil(lockedUntil);
            appUserRepository.save(user);

            auditService.recordAuthEvent(
                user.getId(),
                user.getEmail(),
                resolveBranchIdNullable(user.getId()),
                "AUTH_LOCKOUT",
                "app_user",
                user.getId(),
                Map.of("failedAttempts", failedCount, "lockedUntil", lockedUntil)
            );
        }
    }

    private void recordSuccessfulAttempt(AppUserEntity user, String username) {
        AuthLoginAttemptEntity attempt = new AuthLoginAttemptEntity();
        attempt.setId(UUID.randomUUID());
        attempt.setUserId(user.getId());
        attempt.setUsername(username == null ? "-" : username.trim());
        attempt.setIp(currentIp());
        attempt.setSuccessful(true);
        attempt.setCreatedAt(OffsetDateTime.now());
        authLoginAttemptRepository.save(attempt);
    }

    private void clearLockIfExpired(AppUserEntity user) {
        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(OffsetDateTime.now())) {
            user.setLockedUntil(null);
            appUserRepository.save(user);
            auditService.recordAuthEvent(
                user.getId(),
                user.getEmail(),
                resolveBranchIdNullable(user.getId()),
                "AUTH_UNLOCK",
                "app_user",
                user.getId(),
                Map.of("reason", "LOCK_EXPIRED")
            );
        }
    }

    private boolean isTwoFaRequired(String roleCode) {
        if (!hardeningProperties.getTwoFa().isEnforcementEnabled()) {
            return false;
        }
        return "ADMIN".equals(roleCode) || "SUPERADMIN".equals(roleCode);
    }

    private void requireAdminOrSuperadmin(String roleCode) {
        if (!"ADMIN".equals(roleCode) && !"SUPERADMIN".equals(roleCode)) {
            throw new ApiProblemException(
                HttpStatus.FORBIDDEN,
                "https://sassveterinaria.local/errors/forbidden",
                "Forbidden",
                "Solo ADMIN/SUPERADMIN pueden operar 2FA.",
                "FORBIDDEN"
            );
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot hash token", ex);
        }
    }

    private ApiProblemException invalidCredentials() {
        return new ApiProblemException(
            HttpStatus.UNAUTHORIZED,
            "https://sassveterinaria.local/errors/invalid-credentials",
            "Invalid credentials",
            "Credenciales invalidas.",
            "INVALID_CREDENTIALS"
        );
    }

    private String currentIp() {
        org.springframework.web.context.request.RequestAttributes attrs =
            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttrs)) {
            return null;
        }
        String forwardedFor = servletAttrs.getRequest().getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int comma = forwardedFor.indexOf(',');
            return comma >= 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        return servletAttrs.getRequest().getRemoteAddr();
    }

    private record ChallengeBundle(String token, OffsetDateTime expiresAt) {
    }
}

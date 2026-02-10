package com.sassveterinaria.auth.service;

import com.sassveterinaria.auth.domain.AppUserEntity;
import com.sassveterinaria.auth.domain.BranchEntity;
import com.sassveterinaria.auth.domain.RefreshTokenEntity;
import com.sassveterinaria.auth.domain.UserBranchEntity;
import com.sassveterinaria.auth.dto.AuthResponse;
import com.sassveterinaria.auth.dto.LoginRequest;
import com.sassveterinaria.auth.dto.RefreshRequest;
import com.sassveterinaria.auth.repo.AppUserRepository;
import com.sassveterinaria.auth.repo.BranchRepository;
import com.sassveterinaria.auth.repo.RefreshTokenRepository;
import com.sassveterinaria.auth.repo.UserBranchRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.JwtProperties;
import com.sassveterinaria.security.JwtService;
import com.sassveterinaria.security.PermissionMatrix;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(
        AppUserRepository appUserRepository,
        UserBranchRepository userBranchRepository,
        BranchRepository branchRepository,
        RefreshTokenRepository refreshTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        JwtProperties jwtProperties
    ) {
        this.appUserRepository = appUserRepository;
        this.userBranchRepository = userBranchRepository;
        this.branchRepository = branchRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
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

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            throw new ApiProblemException(
                HttpStatus.FORBIDDEN,
                "https://sassveterinaria.local/errors/user-locked",
                "User locked",
                "El usuario esta bloqueado temporalmente.",
                "USER_LOCKED"
            );
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
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

        return buildAuthResponse(user, branch, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
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

        return buildAuthResponse(user, branch, accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        String refreshHash = hashToken(refreshToken);
        OffsetDateTime now = OffsetDateTime.now();
        refreshTokenRepository.findActiveByTokenHash(refreshHash, now).ifPresent(token -> {
            token.setRevokedAt(now);
            refreshTokenRepository.save(token);
        });
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
}

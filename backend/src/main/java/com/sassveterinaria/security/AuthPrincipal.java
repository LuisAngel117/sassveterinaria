package com.sassveterinaria.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AuthPrincipal {

    private final UUID userId;
    private final String username;
    private final String fullName;
    private final String roleCode;
    private final UUID branchId;
    private final List<String> permissions;

    public AuthPrincipal(
        UUID userId,
        String username,
        String fullName,
        String roleCode,
        UUID branchId,
        List<String> permissions
    ) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.roleCode = roleCode;
        this.branchId = branchId;
        this.permissions = permissions;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream().map(SimpleGrantedAuthority::new).toList();
    }
}

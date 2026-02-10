package com.sassveterinaria.security;

import java.util.List;
import java.util.Map;

public final class PermissionMatrix {

    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
        "SUPERADMIN", List.of("BRANCH_SELECT", "BRANCH_READ", "APPT_READ", "APPT_CREATE"),
        "ADMIN", List.of("BRANCH_SELECT", "BRANCH_READ", "APPT_READ", "APPT_CREATE"),
        "RECEPCION", List.of("BRANCH_SELECT", "BRANCH_READ", "APPT_READ", "APPT_CREATE"),
        "VETERINARIO", List.of("BRANCH_SELECT", "BRANCH_READ", "APPT_READ")
    );

    private PermissionMatrix() {
    }

    public static List<String> permissionsForRole(String roleCode) {
        return ROLE_PERMISSIONS.getOrDefault(roleCode, List.of());
    }
}

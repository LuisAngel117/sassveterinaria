package com.sassveterinaria.security;

import java.util.List;
import java.util.Map;

public final class PermissionMatrix {

    private static final List<String> ADMIN_PERMISSIONS = List.of(
        "BRANCH_SELECT",
        "BRANCH_READ",
        "BRANCH_MANAGE",
        "APPT_READ",
        "APPT_CREATE",
        "APPT_UPDATE",
        "APPT_CANCEL",
        "APPT_OVERBOOK",
        "APPT_CHECKIN",
        "APPT_START_VISIT",
        "APPT_CLOSE"
    );

    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
        "SUPERADMIN", ADMIN_PERMISSIONS,
        "ADMIN", ADMIN_PERMISSIONS,
        "RECEPCION", List.of(
            "BRANCH_SELECT",
            "BRANCH_READ",
            "APPT_READ",
            "APPT_CREATE",
            "APPT_UPDATE",
            "APPT_CANCEL",
            "APPT_CHECKIN"
        ),
        "VETERINARIO", List.of(
            "BRANCH_SELECT",
            "BRANCH_READ",
            "APPT_READ",
            "APPT_CHECKIN",
            "APPT_START_VISIT",
            "APPT_CLOSE"
        )
    );

    private PermissionMatrix() {
    }

    public static List<String> permissionsForRole(String roleCode) {
        return ROLE_PERMISSIONS.getOrDefault(roleCode, List.of());
    }
}

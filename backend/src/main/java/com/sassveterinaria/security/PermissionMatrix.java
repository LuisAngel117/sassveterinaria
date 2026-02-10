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
        "APPT_CLOSE",
        "CLIENT_READ",
        "CLIENT_CREATE",
        "CLIENT_UPDATE",
        "PET_READ",
        "PET_CREATE",
        "PET_UPDATE",
        "SERVICE_READ",
        "SERVICE_CREATE",
        "SERVICE_UPDATE"
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
            "APPT_CHECKIN",
            "CLIENT_READ",
            "CLIENT_CREATE",
            "CLIENT_UPDATE",
            "PET_READ",
            "PET_CREATE",
            "PET_UPDATE",
            "SERVICE_READ"
        ),
        "VETERINARIO", List.of(
            "BRANCH_SELECT",
            "BRANCH_READ",
            "APPT_READ",
            "APPT_CHECKIN",
            "APPT_START_VISIT",
            "APPT_CLOSE",
            "CLIENT_READ",
            "PET_READ",
            "SERVICE_READ"
        )
    );

    private PermissionMatrix() {
    }

    public static List<String> permissionsForRole(String roleCode) {
        return ROLE_PERMISSIONS.getOrDefault(roleCode, List.of());
    }
}

package com.sassveterinaria.security;

import java.util.List;
import java.util.Map;

public final class PermissionMatrix {

    private static final List<String> ADMIN_PERMISSIONS = List.of(
        "BRANCH_SELECT",
        "BRANCH_READ",
        "BRANCH_MANAGE",
        "CONFIG_TAX_READ",
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
        "SERVICE_UPDATE",
        "VISIT_READ",
        "VISIT_CREATE",
        "VISIT_UPDATE",
        "VISIT_CLOSE",
        "VISIT_REOPEN",
        "VISIT_ATTACHMENT_UPLOAD",
        "INVOICE_READ",
        "INVOICE_CREATE",
        "INVOICE_UPDATE",
        "INVOICE_PAY",
        "INVOICE_VOID",
        "INVOICE_EXPORT",
        "REPORT_READ",
        "REPORT_EXPORT",
        "AUDIT_READ",
        "PRODUCT_READ",
        "PRODUCT_CREATE",
        "PRODUCT_UPDATE",
        "STOCK_READ",
        "STOCK_MOVE_CREATE",
        "STOCK_ADJUST",
        "STOCK_OVERRIDE_INVOICE"
    );

    private static final List<String> SUPERADMIN_PERMISSIONS = List.of(
        "BRANCH_SELECT",
        "BRANCH_READ",
        "BRANCH_MANAGE",
        "CONFIG_TAX_READ",
        "CONFIG_TAX_UPDATE",
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
        "SERVICE_UPDATE",
        "VISIT_READ",
        "VISIT_CREATE",
        "VISIT_UPDATE",
        "VISIT_CLOSE",
        "VISIT_REOPEN",
        "VISIT_ATTACHMENT_UPLOAD",
        "INVOICE_READ",
        "INVOICE_CREATE",
        "INVOICE_UPDATE",
        "INVOICE_PAY",
        "INVOICE_VOID",
        "INVOICE_EXPORT",
        "REPORT_READ",
        "REPORT_EXPORT",
        "AUDIT_READ",
        "PRODUCT_READ",
        "PRODUCT_CREATE",
        "PRODUCT_UPDATE",
        "STOCK_READ",
        "STOCK_MOVE_CREATE",
        "STOCK_ADJUST",
        "STOCK_OVERRIDE_INVOICE"
    );

    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
        "SUPERADMIN", SUPERADMIN_PERMISSIONS,
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
            "SERVICE_READ",
            "INVOICE_READ",
            "INVOICE_CREATE",
            "INVOICE_PAY",
            "REPORT_READ"
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
            "SERVICE_READ",
            "VISIT_READ",
            "VISIT_CREATE",
            "VISIT_UPDATE",
            "VISIT_CLOSE",
            "VISIT_REOPEN",
            "VISIT_ATTACHMENT_UPLOAD",
            "INVOICE_READ",
            "PRODUCT_READ",
            "STOCK_READ",
            "REPORT_READ",
            "AUDIT_READ"
        )
    );

    private PermissionMatrix() {
    }

    public static List<String> permissionsForRole(String roleCode) {
        return ROLE_PERMISSIONS.getOrDefault(roleCode, List.of());
    }
}

# 10 - Permisos (matriz estable)

## Roles
- SUPERADMIN: control global del sistema y configuraciones criticas.
- ADMIN: gestion completa de sucursal asignada.
- RECEPCION: agenda, clientes, check-in, facturacion operativa.
- VETERINARIO: atencion clinica y cierre SOAP.

## Permisos por modulo

### Auth y sucursal
- AUTH_LOGIN
- AUTH_2FA_VERIFY
- BRANCH_SELECT

### Usuarios y configuracion
- USER_VIEW
- USER_CREATE
- USER_UPDATE
- USER_BLOCK
- ROLE_ASSIGN
- TAX_RATE_UPDATE

### Agenda y pacientes
- APPOINTMENT_VIEW
- APPOINTMENT_CREATE
- APPOINTMENT_RESCHEDULE
- APPOINTMENT_CANCEL
- APPOINTMENT_CHECKIN
- CLIENT_VIEW
- CLIENT_CREATE
- CLIENT_UPDATE
- PET_VIEW
- PET_CREATE
- PET_UPDATE

### Historia clinica
- SOAP_VIEW
- SOAP_EDIT
- SOAP_CLOSE
- SOAP_REOPEN
- SOAP_ATTACH_FILE

### Facturacion
- INVOICE_VIEW
- INVOICE_ISSUE
- INVOICE_VOID

### Inventario
- INVENTORY_VIEW
- INVENTORY_ADJUST
- INVENTORY_COST_OVERRIDE
- BOM_MANAGE

### Auditoria y reportes
- AUDIT_VIEW
- REPORT_VIEW

## Matriz rol -> permisos
| Permiso | SUPERADMIN | ADMIN | RECEPCION | VETERINARIO |
|---|---|---|---|---|
| AUTH_LOGIN | X | X | X | X |
| AUTH_2FA_VERIFY | X | X |  |  |
| BRANCH_SELECT | X | X | X | X |
| USER_VIEW | X | X |  |  |
| USER_CREATE | X | X |  |  |
| USER_UPDATE | X | X |  |  |
| USER_BLOCK | X | X |  |  |
| ROLE_ASSIGN | X | X |  |  |
| TAX_RATE_UPDATE | X |  |  |  |
| APPOINTMENT_VIEW | X | X | X | X |
| APPOINTMENT_CREATE | X | X | X |  |
| APPOINTMENT_RESCHEDULE | X | X | X | X |
| APPOINTMENT_CANCEL | X | X | X | X |
| APPOINTMENT_CHECKIN | X | X | X |  |
| CLIENT_VIEW | X | X | X | X |
| CLIENT_CREATE | X | X | X |  |
| CLIENT_UPDATE | X | X | X |  |
| PET_VIEW | X | X | X | X |
| PET_CREATE | X | X | X |  |
| PET_UPDATE | X | X | X | X |
| SOAP_VIEW | X | X |  | X |
| SOAP_EDIT | X | X |  | X |
| SOAP_CLOSE | X | X |  | X |
| SOAP_REOPEN | X | X |  | X |
| SOAP_ATTACH_FILE | X | X |  | X |
| INVOICE_VIEW | X | X | X | X |
| INVOICE_ISSUE | X | X | X |  |
| INVOICE_VOID | X | X |  |  |
| INVENTORY_VIEW | X | X | X | X |
| INVENTORY_ADJUST | X | X |  |  |
| INVENTORY_COST_OVERRIDE | X | X |  |  |
| BOM_MANAGE | X | X |  |  |
| AUDIT_VIEW | X | X |  |  |
| REPORT_VIEW | X | X | X | X |

## Acciones sensibles (reason required)
- `SOAP_REOPEN`
- `INVOICE_VOID`
- `INVENTORY_ADJUST`
- `INVENTORY_COST_OVERRIDE`
- `ROLE_ASSIGN` hacia roles de alto privilegio
- `TAX_RATE_UPDATE`

<!-- EOF -->

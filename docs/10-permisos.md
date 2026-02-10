# 10 — Permisos (matriz estable)

## 1) Roles v1

- SUPERADMIN: control total (demo/soporte). Puede operar cualquier sucursal (requiere `X-Branch-Id` igualmente).
- ADMIN: administra operación de clínica (usuarios, configuración, reportes).
- RECEPCION: agenda, clientes, cobros básicos (según permisos).
- VETERINARIO: atención clínica, historia clínica, indicaciones.

## 2) Convención de permisos

- Código en inglés, estilo: `MODULE_ACTION_SCOPE`
- Ejemplos:
  - `APPT_CREATE`
  - `INVOICE_VOID`
  - `INVENTORY_OVERRIDE_STOCK`

## 3) Permisos (lista mínima v1)

### Auth / Admin
- `USER_READ`
- `USER_CREATE`
- `USER_UPDATE`
- `USER_DISABLE`
- `ROLE_ASSIGN`
- `CONFIG_TAX_READ`
- `CONFIG_TAX_UPDATE` (SENSITIVE)

### Branch / Scope
- `BRANCH_READ`
- `BRANCH_SELECT` (selección de contexto)
- `BRANCH_MANAGE` (admin)

### Agenda
- `APPT_READ`
- `APPT_CREATE`
- `APPT_UPDATE`
- `APPT_CANCEL` (SENSITIVE)
- `APPT_OVERBOOK` (SENSITIVE)
- `APPT_CHECKIN`
- `APPT_START_VISIT`
- `APPT_CLOSE`

### Clientes / Mascotas
- `CLIENT_READ`
- `CLIENT_CREATE`
- `CLIENT_UPDATE`
- `PET_READ`
- `PET_CREATE`
- `PET_UPDATE`

### Historia Clínica / Atenciones
- `VISIT_READ`
- `VISIT_CREATE`
- `VISIT_UPDATE`
- `VISIT_CLOSE`
- `VISIT_REOPEN` (SENSITIVE)
- `VISIT_ATTACHMENT_UPLOAD`

### Servicios
- `SERVICE_READ`
- `SERVICE_CREATE`
- `SERVICE_UPDATE` (SENSITIVE si cambia precio)

### Facturación
- `INVOICE_READ`
- `INVOICE_CREATE`
- `INVOICE_UPDATE` (SENSITIVE si cambia precio/descuento)
- `INVOICE_PAY`
- `INVOICE_VOID` (SENSITIVE)
- `INVOICE_EXPORT`

### Inventario
- `PRODUCT_READ`
- `PRODUCT_CREATE`
- `PRODUCT_UPDATE`
- `STOCK_READ`
- `STOCK_MOVE_CREATE` (ingreso/egreso)
- `STOCK_ADJUST` (SENSITIVE)
- `STOCK_OVERRIDE_INVOICE` (SENSITIVE)

### Reportes
- `REPORT_READ`
- `REPORT_EXPORT`

### Auditoría
- `AUDIT_READ`

## 4) Matriz rol → permisos (v1)

| Permiso | SUPERADMIN | ADMIN | RECEPCION | VETERINARIO |
|---|:--:|:--:|:--:|:--:|
| USER_* / ROLE_ASSIGN | ✅ | ✅ | ❌ | ❌ |
| CONFIG_TAX_* | ✅ | ✅(read) | ❌ | ❌ |
| BRANCH_* | ✅ | ✅ | ✅(select/read) | ✅(select/read) |
| APPT_* | ✅ | ✅ | ✅ | ✅(read + start/close) |
| CLIENT_* / PET_* | ✅ | ✅ | ✅ | ✅(read) |
| VISIT_* | ✅ | ✅ | ❌ | ✅ |
| SERVICE_* | ✅ | ✅ | ✅(read) | ✅(read) |
| INVOICE_* | ✅ | ✅ | ✅(create/pay/read) | ✅(read) |
| INVENTORY_* | ✅ | ✅ | ❌ | ✅(read) |
| REPORT_* | ✅ | ✅ | ✅(read limitado) | ✅(read limitado) |
| AUDIT_READ | ✅ | ✅ | ❌ | ✅(solo sus acciones si se implementa filtro) |

## 5) Acciones sensibles (reason required)

- `INVOICE_VOID`
- `INVOICE_UPDATE` cuando cambia precio/descuento
- `SERVICE_UPDATE` cuando cambia precio
- `VISIT_REOPEN`
- `APPT_OVERBOOK`
- `APPT_CANCEL` (si ya estaba confirmado/en atención)
- `STOCK_ADJUST`
- `STOCK_OVERRIDE_INVOICE`
- `CONFIG_TAX_UPDATE`

<!-- EOF -->

# Plan Maestro BACK (spr-master-back)

Regla: este master se acepta “tal cual”. Cambios solo por RFC/ADR/CHANGELOG.

## Lista de sprints (BACK)

### SPR-B001 — Walking skeleton backend (health + db + migraciones + ProblemDetails base)
- Hace: estructura backend, conexión Postgres, migraciones base, healthcheck, error RFC7807 base
- No hace: features de dominio completas
- Riesgo: inventar paths/commands; mitigación: leer índice/runbook
- Dep: ninguna
- BRD objetivo: BRD-REQ-015, BRD-REQ-102

### SPR-B002 — Auth + scoping branch + permisos base + 2FA base
- Hace: login/refresh/logout, selección branch, claims+header, roles/permisos, lockout, 2FA TOTP
- No hace: agenda/crm
- Dep: B001
- BRD: 001-003, 010-015, 020-022

### SPR-B003 — Catálogos + seeds demo base (branches/rooms/services/products/units)
- Hace: CRUD catálogos + seeds (usuarios demo incluidos)
- Dep: B002
- BRD: 030, 060, 080-082, 100

### SPR-B004 — Agenda (citas + no-solape + buffer + check-in)
- Dep: B003
- BRD: 031-035

### SPR-B005 — CRM (clientes + mascotas + alertas)
- Dep: B003
- BRD: 040-043

### SPR-B006 — Clínica (atenciones SOAP + plantillas + cierre/reapertura + adjuntos metadata)
- Dep: B005
- BRD: 050-055, 053

### SPR-B007 — Facturación (factura demo + IVA config + pagos mixtos/parciales + anulación + export)
- Dep: B006
- BRD: 070-076, 071-075

### SPR-B008 — Inventario (stock+movimientos+consumo BOM+mínimos+override)
- Dep: B007
- BRD: 080-087, 063

### SPR-B009 — Reportes (citas/ventas/top/consumo/frecuentes + export)
- Dep: B008
- BRD: 090-096

### SPR-B010 — Hardening auditoría + políticas sensibles + retención
- Dep: B009
- BRD: 020-023, 021

### SPR-B011 — OpenAPI polish + smoke scripts backend + handoff base
- Dep: B010
- BRD: 101-103

### SPR-RC001 — Release Candidate local (scripts verify/smoke/release-candidate + checklist entrega)
- Dep: B011 (y FRONT listo)
- BRD: 102-103

<!-- EOF -->

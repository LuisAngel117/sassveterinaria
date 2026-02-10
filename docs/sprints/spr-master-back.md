# Plan Maestro — BACK (sprints backend)

Regla: este master se acepta “tal cual”. Cambios solo por RFC/ADR.

## Convenciones

- IDs: `SPR-B001`, `SPR-B002`, ...
- Cada sprint declara BRD-REQ objetivo.
- Si un sprint es “Foundation”, debe justificar desbloqueo.

## Lista de sprints

### SPR-B001 — Walking Skeleton (Auth + Scope + Base API + Smoke mínimo)
- Objetivo: tener un flujo mínimo backend usable: auth + scoping + 1 caso core persistido.
- Cierra: BRD-REQ-001,002,003,007,008,057
- Incluye:
  - JWT access/refresh (rotación)
  - Scope `X-Branch-Id` validado contra claims
  - Endpoint mínimo de agenda (crear/listar cita simple) + healthcheck
  - Base Problem Details
  - Seed mínimo (usuarios demo básicos)
- Excluye: reglas completas de no-solape y clínica (viene después)
- Depende: docs base (BRD/arquitectura/seguridad) cerrados

### SPR-B002 — Agenda Core (no-solape por sala + estados + check-in)
- Cierra: BRD-REQ-010..015,022,012,013,014
- Incluye:
  - Modelo de cita completo con estados
  - Regla no-solape por SALA (hard)
  - Sobre-cupo con permiso + auditoría
  - Check-in separado
  - Listado calendario semana (API)
- Depende: SPR-B001

### SPR-B003 — Clientes y Mascotas (CRUD + búsqueda + invariantes)
- Cierra: BRD-REQ-016..020
- Depende: SPR-B001

### SPR-B004 — Servicios (catálogo + duración + precio base)
- Cierra: BRD-REQ-021..022
- Depende: SPR-B001

### SPR-B005 — Historia Clínica (Atención + SOAP + plantillas + adjuntos + cierre/reapertura)
- Cierra: BRD-REQ-024..030 (parcial, export puede ir en B006)
- Depende: SPR-B003, SPR-B004, SPR-B001

### SPR-B006 — Facturación (factura + IVA config + pagos + export)
- Cierra: BRD-REQ-031..037,032,033,034,035,036
- Depende: SPR-B005, SPR-B004

### SPR-B007 — Inventario (stock + movimientos + costeo + override + BOM)
- Cierra: BRD-REQ-038..044,023
- Depende: SPR-B006 (si consumo se cruza con facturación) y SPR-B001

### SPR-B008 — Reportes (endpoints + export)
- Cierra: BRD-REQ-045..051
- Depende: SPR-B002, SPR-B006, SPR-B007

### SPR-B009 — Auditoría avanzada (before/after + retención)
- Cierra: BRD-REQ-052..054,032 (auditar cambio IVA)
- Depende: SPR-B001

### SPR-B010 — Hardening de Seguridad (2FA + lockout + rate limit + permisos finos)
- Cierra: BRD-REQ-004,005,006,009
- Depende: SPR-B001

### SPR-B011 — Seeds demo + Smoke scripts flujo core
- Cierra: BRD-REQ-055,056,058
- Depende: SPR-B002, SPR-B003, SPR-B005, SPR-B006, SPR-B007 (mínimos), SPR-B010 (mínimos)

## Nota

- Los sprints `SPR-RC###` se definen después de tener BACK+FRONT integrados en local.

<!-- EOF -->

# Plan Maestro — FRONT (sprints frontend)

Regla: este master se acepta “tal cual”. Cambios solo por RFC/ADR.

## Convenciones

- IDs: `SPR-F001`, `SPR-F002`, ...
- UI en español.
- Integración real contra backend cuando ya exista (no pantallas sueltas).

## Lista de sprints

### SPR-F001 — Shell + Auth UI + Selector de Sucursal
- Cierra: BRD-REQ-001 (UX), 007 (selección), 056 (credenciales demo)
- Incluye:
  - Layout vendible, navegación por rol
  - Login + refresh handling
  - Selector de sucursal al iniciar (basado en branches permitidas)
  - Manejo de errores consistente
- Depende: SPR-B001

### SPR-F002 — Agenda UI (calendario semana + crear/editar + estados + conflictos)
- Cierra: BRD-REQ-010..015
- Incluye:
  - Vista semana, filtros por sala/estado
  - Crear/editar cita, transición estados
  - Mostrar conflicto no-solape y permitir sobre-cupo solo si permiso
  - Check-in separado
- Depende: SPR-B002

### SPR-F003 — Clientes y Mascotas UI (CRUD + búsqueda)
- Cierra: BRD-REQ-016..020
- Depende: SPR-B003

### SPR-F004 — Atención / SOAP UI (plantillas + adjuntos + cierre/reapertura)
- Cierra: BRD-REQ-024..029
- Depende: SPR-B005

### SPR-F005 — Facturación UI (factura + pagos + export)
- Cierra: BRD-REQ-031..037
- Depende: SPR-B006

### SPR-F006 — Inventario UI (stock + alertas + movimientos)
- Cierra: BRD-REQ-038..044
- Depende: SPR-B007

### SPR-F007 — Reportes + Dashboard por rol
- Cierra: BRD-REQ-045..051
- Depende: SPR-B008

### SPR-F008 — Auditoría UI + Reason Modal (acciones sensibles)
- Cierra: BRD-REQ-052..053
- Depende: SPR-B009

### SPR-F009 — Pulido vendible (UX, accesibilidad, performance, errores)
- Cierra: N/A (mejora transversal)
- Depende: SPR-F001..SPR-F008

### SPR-F010 — Demo polish (seed UX, “cómo probar en 2–3 min” en UI)
- Cierra: BRD-REQ-055..056 (apoyo)
- Depende: SPR-B011 + SPR-F001..SPR-F005 mínimo

## Nota

- Los sprints `SPR-RC###` se definen después de integrar BACK+FRONT en local.

<!-- EOF -->

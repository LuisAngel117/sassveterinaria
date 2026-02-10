# Plan Maestro FRONT (spr-master-front)

Regla: este master se acepta “tal cual”. Cambios solo por RFC/ADR/CHANGELOG.
Regla: FRONT NO inventa contratos; requiere handoff real cuando backend exista.

## Lista de sprints (FRONT)

### SPR-F001 — Shell + auth + selección sucursal + layout vendible
- Hace: login, refresh handling, selector branch, navegación base, manejo errores
- Dep: handoff parcial (auth endpoints)
- BRD: 002, 010-011, 096, 101

### SPR-F002 — Agenda integrada (calendario semanal + citas)
- Dep: backend B004 (handoff actualizado)
- BRD: 031-035, 032

### SPR-F003 — CRM (clientes + mascotas)
- Dep: backend B005
- BRD: 040-043

### SPR-F004 — Clínica (atención SOAP + plantillas + cierre/reapertura + adjuntos)
- Dep: backend B006
- BRD: 050-055, 053

### SPR-F005 — Facturación (facturas + pagos + export)
- Dep: backend B007
- BRD: 070-076

### SPR-F006 — Inventario (productos/stock/movimientos/alertas)
- Dep: backend B008
- BRD: 080-087

### SPR-F007 — Reportes + export + dashboard por rol
- Dep: backend B009
- BRD: 090-096

### SPR-F008 — Admin UI (catálogos + IVA config + auditoría view)
- Dep: backend B010
- BRD: 071, 020-022

### SPR-F009 — Pulido vendible (UX/performance/permiso-gating completo)
- Dep: F008
- BRD: 103

### SPR-RC001 — Release Candidate local (compartido)
- Dep: BACK+FRONT listos
- BRD: 102-103

<!-- EOF -->

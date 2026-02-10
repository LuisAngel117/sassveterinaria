# State Snapshot

## Resumen actual (hoy)

- SPR-B001, SPR-B002, SPR-B003 y SPR-B004 implementados en backend.
- Agenda Core (B002) activo con no-solape/estados/check-in/bloqueos.
- CRM base (B003) activo para clientes y mascotas.
- Servicios (B004) activo:
  - CRUD v1 (`POST/GET/GET{id}/PATCH /api/v1/services`)
  - campos `name`, `durationMinutes`, `priceBase`, `isActive`
  - cambio de `priceBase` exige `reason` y audita before/after
  - permisos `SERVICE_READ/CREATE/UPDATE` aplicados por rol
- Se creó smoke script `scripts/smoke/spr-b004.ps1`.

## Estado de sprints (alto nivel)

- SPR-B001: READY_FOR_VALIDATION.
- SPR-B002: READY_FOR_VALIDATION.
- SPR-B003: READY_FOR_VALIDATION.
- SPR-B004: READY_FOR_VALIDATION.
- Proximo sprint recomendado: SPR-B005 (Historia Clinica).

## Riesgos/bloqueos actuales

- Smokes B002/B003/B004 requieren backend corriendo con PostgreSQL local y datos seed demo.
- La validacion funcional final (READY_FOR_VALIDATION -> DONE) depende de ejecucion local del usuario y evidencia en LOG.

<!-- EOF -->

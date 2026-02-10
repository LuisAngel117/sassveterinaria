# State Snapshot

## Resumen actual (hoy)

- SPR-B001, SPR-B002, SPR-B003, SPR-B004 y SPR-B005 implementados en backend.
- Agenda Core (B002) activo con no-solape/estados/check-in/bloqueos.
- CRM base (B003) activo para clientes y mascotas.
- Servicios (B004) activo:
  - CRUD v1 (`POST/GET/GET{id}/PATCH /api/v1/services`)
  - campos `name`, `durationMinutes`, `priceBase`, `isActive`
  - cambio de `priceBase` exige `reason` y audita before/after
  - permisos `SERVICE_READ/CREATE/UPDATE` aplicados por rol
- Historia clinica (B005) activo:
  - visitas `OPEN/CLOSED` (walk-in o vinculadas a cita)
  - SOAP minimo y plantillas por servicio
  - prescripciones estructuradas por visita
  - adjuntos pdf/jpg/png con limites configurables y almacenamiento local (`STORAGE_DIR`)
  - reapertura con `VISIT_REOPEN` + reason + auditoria before/after
- Se creó smoke script `scripts/smoke/spr-b005.ps1`.

## Estado de sprints (alto nivel)

- SPR-B001: READY_FOR_VALIDATION.
- SPR-B002: READY_FOR_VALIDATION.
- SPR-B003: READY_FOR_VALIDATION.
- SPR-B004: READY_FOR_VALIDATION.
- SPR-B005: READY_FOR_VALIDATION.
- Proximo sprint recomendado: SPR-B006 (Facturacion).

## Riesgos/bloqueos actuales

- Smokes B002/B003/B004/B005 requieren backend corriendo con PostgreSQL local y datos seed demo.
- La validacion funcional final (READY_FOR_VALIDATION -> DONE) depende de ejecucion local del usuario y evidencia en LOG.

<!-- EOF -->

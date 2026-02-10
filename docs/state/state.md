# State Snapshot

## Resumen actual (hoy)

- SPR-B001 implementado en backend con alcance de walking skeleton.
- Se creó `backend/` con Spring Boot + Flyway + PostgreSQL + Security + OpenAPI.
- Auth implementado: login, refresh rotativo, logout, me.
- Scope por sucursal implementado con `X-Branch-Id` vs claim `branch_id`.
- Caso core mínimo implementado: crear/listar citas por branch.
- Se creó smoke script `scripts/smoke/spr-b001.ps1`.

## Estado de sprints (alto nivel)

- SPR-B001: READY_FOR_VALIDATION.
- Próximo sprint recomendado: SPR-B002 (Agenda Core: no-solape por sala + estados + check-in).

## Riesgos/bloqueos actuales

- Ejecución de smoke end-to-end depende de backend corriendo con PostgreSQL local.
- Validación funcional final queda para ejecución local del usuario con evidencia en LOG.

<!-- EOF -->

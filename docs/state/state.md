# State Snapshot

## Resumen actual (hoy)

- SPR-B001 y SPR-B002 implementados en backend.
- Agenda Core disponible:
  - rooms branch-scoped
  - services minimos con duracion para agenda
  - appointments con estados (`RESERVED`, `CONFIRMED`, `IN_ATTENTION`, `CLOSED`, `CANCELLED`)
  - no-solape por sala con excepcion de sobre-cupo (`APPT_OVERBOOK` + reason + auditoria)
  - check-in separado de cambio de estado
  - bloqueos manuales por sala (`room_block`)
- Se creó smoke script `scripts/smoke/spr-b002.ps1`.

## Estado de sprints (alto nivel)

- SPR-B001: READY_FOR_VALIDATION.
- SPR-B002: READY_FOR_VALIDATION.
- Proximo sprint recomendado: SPR-B003 (Clientes y Mascotas).

## Riesgos/bloqueos actuales

- El smoke B002 requiere backend corriendo con PostgreSQL local y datos seed de branch/usuarios.
- La validacion funcional final (READY_FOR_VALIDATION -> DONE) depende de ejecucion local del usuario y evidencia en LOG.

<!-- EOF -->

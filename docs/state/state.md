# State Snapshot

## Resumen actual (hoy)

- SPR-B001, SPR-B002 y SPR-B003 implementados en backend.
- Agenda Core (B002) sigue activo con no-solape/estados/check-in/bloqueos.
- CRM base (B003) disponible:
  - clientes: create/list+search/detail/patch
  - mascotas: create/list/detail/patch
  - busqueda de clientes por `fullName`/`phone`/`identification`
  - unicidad de `pet.internalCode` por sucursal
  - invariante v1: 1 mascota -> 1 propietario por `client_id`
- Se creó smoke script `scripts/smoke/spr-b003.ps1`.

## Estado de sprints (alto nivel)

- SPR-B001: READY_FOR_VALIDATION.
- SPR-B002: READY_FOR_VALIDATION.
- SPR-B003: READY_FOR_VALIDATION.
- Proximo sprint recomendado: SPR-B004 (Servicios catalogo completo).

## Riesgos/bloqueos actuales

- Smokes B002/B003 requieren backend corriendo con PostgreSQL local y datos seed demo.
- La validacion funcional final (READY_FOR_VALIDATION -> DONE) depende de ejecucion local del usuario y evidencia en LOG.

<!-- EOF -->

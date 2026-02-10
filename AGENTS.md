# AGENTS

## Objetivo
Mantener consistencia tecnica en documentacion, trazabilidad y scripts de verificacion.

## Reglas operativas
- Trabajar en rama `main` hasta definir flujo de ramas en `docs/04-convenciones.md`.
- Antes de validar cambios, ejecutar `pwsh -File scripts/verify/preflight.ps1`.
- No cerrar tareas como `DONE` en `docs/status/status.md`; usar `READY_FOR_VALIDATION` hasta validacion manual.
- Mantener trazabilidad en `docs/log/log.md`, `docs/changelog.md` y `docs/traceability/rtm.md`.

## Cobertura actual
- Esta base T1 crea el marco de gobernanza, control de calidad documental y decision records.
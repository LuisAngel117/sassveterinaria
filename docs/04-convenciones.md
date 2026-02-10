# 04 - Convenciones

## Nomenclatura
- Archivos markdown numerados por orden de lectura.
- ADRs con formato `adr-000N-*`.
- RFCs con formato `rfc-000N-*`.

## Flujo de trabajo
- Estado de ejecucion en `docs/status/status.md`.
- Registro append-only en `docs/log/log.md`.
- Cambios relevantes en `docs/changelog.md`.

## Calidad documental
- Todos los `.md` dentro de `docs/**` deben terminar con `<!-- EOF -->`.
- No usar estados finales en status sin validacion manual.

## Convencion de estado
- `READY_FOR_VALIDATION`: entregable listo para revision.
- `BLOCKED`: dependencia externa impide avance.
- `IN_PROGRESS`: trabajo en ejecucion.

<!-- EOF -->
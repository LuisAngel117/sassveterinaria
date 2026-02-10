# 04 - Convenciones (normas duras)

## Naming
- Carpetas y archivos: minusculas, kebab-case, sin espacios.
- Entidades DB: snake_case.
- Campos JSON/API: camelCase.
- IDs de requisitos: `BRD-REQ-###` y `BRD-NFR-###`.
- IDs de sprint: `SPR-B###` (back) y `SPR-F###` (front).

## Formateo
- Markdown con encabezados claros y tablas para matrices.
- Todas las rutas en docs se escriben con path real del repo.
- Todo `.md` de `docs/**` termina exacto con `<!-- EOF -->`.
- Evitar texto ambiguo: usar reglas verificables.

## Git y commits
- Commits pequenos y trazables por tanda/sprint.
- Mensajes recomendados:
  - `T#: <resumen>` para tandas de docs.
  - `SPR-B###: <resumen>` para sprints backend.
  - `SPR-F###: <resumen>` para sprints frontend.
- No reescribir historial compartido sin instruccion explicita.

## Branching
- Rama principal: `main`.
- Tandas de gobernanza pueden ejecutarse en `main` si el usuario lo define.
- Sprints funcionales recomendados en ramas de trabajo y merge controlado.

## Logs y trazabilidad
- `docs/log/log.md` es append-only.
- `docs/status/status.md` conserva historico de items.
- `docs/changelog.md` registra cambios de alto nivel.
- `docs/traceability/rtm.md` conecta requisito -> sprint -> evidencia.

## Linux strict
- Evitar archivos duplicados por casing (`Log.md` vs `log.md`).
- Evitar espacios y caracteres problematicos en nombres.
- Mantener referencias de path exactas para compatibilidad cross-platform.

<!-- EOF -->

# AGENTS — Conducta obligatoria para agentes (Codex)

Este archivo define “cómo trabajar” sin depender del chat.

## 1) Identidad / anti-mezcla

ANTES de cualquier acción:
1) Leer `docs/project-lock.md`
2) Confirmar que `git remote -v` coincide con `repo_url`
3) Si no coincide: DETENER (repo mismatch)

## 2) Orden mínimo de lectura

- `docs/project-lock.md`
- `docs/00-indice.md`
- `docs/state/state.md`
- Sprint actual (si aplica)
- `docs/status/status.md`
- `docs/log/log.md`
- RFC/ADR que el sprint referencie

## 3) Reglas duras

- NO inventar requisitos, rutas, usuarios, comandos.
- NO tocar archivos fuera del scope indicado.
- Sprints son inmutables: NO se editan. Cambios solo por RFC/ADR/CHANGELOG.
- `docs/log/log.md` es append-only: nunca reescribir entradas.
- `docs/status/status.md`: nunca marcar DONE (solo usuario con validación local).
- Si falta información crítica:
  - crear RFC (y ADR si afecta arquitectura/seguridad/contratos)
  - y DETENER si bloquea

## 4) Quality Gates

- Antes de iniciar un sprint: validar DoR (`docs/quality/definition-of-ready.md`)
- Antes de cerrar a READY_FOR_VALIDATION: validar DoD (`docs/quality/definition-of-done.md`)
- Si falla: dejar BLOCKED / IN_PROGRESS y registrar en LOG.

## 5) Pre-check obligatorio (siempre)

- `git status` limpio o DETENER
- `git config user.name` y `git config user.email` presentes o DETENER
- `git remote -v` coincide o DETENER
- reportar branch

## 6) Evidencia

- Registrar comandos y outputs en `docs/log/log.md`
- Actualizar `docs/status/status.md` con evidencia (hash commit) cuando aplique
- Actualizar RTM/state si el sprint toca requisitos

<!-- EOF -->

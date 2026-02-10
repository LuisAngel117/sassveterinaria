# 00 — Índice canónico (fuente de verdad) + orden de lectura obligatorio

## 1) Regla central
- TODO el contexto vive en `docs/**`.
- La conversación NO es fuente de verdad.
- Los cambios “de plan” se hacen SOLO por RFC/ADR/CHANGELOG.
- Linux strict: nombres sin espacios, case consistente, kebab-case.

## 2) Orden de lectura obligatorio (agentes)
1) `docs/project-lock.md`
2) `docs/00-indice.md`
3) `docs/state/state.md`
4) `docs/status/status.md`
5) `docs/log/log.md`
6) `docs/quality/definition-of-ready.md` y `docs/quality/definition-of-done.md`
7) `docs/traceability/rtm.md`
8) `docs/decisions/*.md` (ADRs)
9) `docs/rfcs/*.md` (si aplica)
10) `docs/sprints/spr-master-back.md` o `spr-master-front.md`
11) Sprint actual `docs/sprints/<SPR-XXX>.md`

## 3) Mapa de documentos (canónico)
| Archivo | Propósito | Leer antes de codificar |
|---|---|---|
| docs/project-lock.md | Identidad del proyecto (anti-mezcla) | Sí |
| docs/00-indice.md | Mapa + reglas + orden lectura | Sí |
| docs/01-brief.md | Contexto negocio + visión | Sí |
| docs/02-brd.md | Requisitos con IDs BRD-REQ-### | Sí |
| docs/03-arquitectura.md | Arquitectura + contratos + scoping | Sí |
| docs/04-convenciones.md | Normas duras (naming, git, scripts) | Sí |
| docs/05-seguridad.md | AuthN/AuthZ/2FA/auditoría | Sí |
| docs/06-dominio-parte-a.md | Modelo de datos (parte A) | Sí |
| docs/06-dominio-parte-b.md | Modelo de datos (parte B) | Sí |
| docs/07-ux-ui-parte-a.md | UX/UI (parte A) | Sí (front) |
| docs/07-ux-ui-parte-b.md | UX/UI (parte B) | Sí (front) |
| docs/08-runbook.md | Cómo correr local + troubleshooting | Sí |
| docs/09-stage-release.md | Stage/online (futuro) | No |
| docs/10-permisos.md | Matriz roles/permisos | Sí |
| docs/11-entrega.md | Checklist vendible local | Sí |
| docs/changelog.md | Registro de cambios | Sí |
| docs/status/status.md | Control de sprints/tandas | Sí |
| docs/log/log.md | Bitácora append-only | Sí |
| docs/quality/definition-of-ready.md | Gate DoR | Sí |
| docs/quality/definition-of-done.md | Gate DoD | Sí |
| docs/traceability/rtm.md | RTM req→sprint→evidencia | Sí |
| docs/state/state.md | Snapshot estado (entrada multi-conversación) | Sí |
| docs/handoff/handoff-back-to-front.md | Handoff real back→front | Sí (front) |
| docs/decisions/adr-*.md | Decisiones arquitectura/seguridad | Sí |
| docs/rfcs/*.md | Cambios propuestos (gobernanza) | Sí |
| docs/sprints/*.md | Plan maestro + sprints inmutables | Sí |

## 4) Nombres canónicos (Linux-friendly)
- Paths canónicos (minúsculas) definidos aquí.
- Si en algún lugar aparece LOG.md/STATUS.md por historia, se interpreta como:
  - `docs/log/log.md`
  - `docs/status/status.md`

## 5) Regla anti-truncado EOF
- TODO `.md` bajo `docs/**` termina EXACTO con:
  `<!-- EOF -->`
- Verificador:
  - `scripts/verify/verify-docs-eof.ps1`
- Preflight:
  - `scripts/verify/preflight.ps1`

## 6) Gobernanza de cambios (inmutable)
- Sprints (`docs/sprints/*.md`) son INMUTABLES.
- Cambios SOLO por:
  - RFC: `docs/rfcs/RFC-00xx-<tema>.md`
  - ADR: `docs/decisions/adr-00xx-<tema>.md`
  - `docs/changelog.md`
- Si hay contradicción, NO se edita el sprint: se crea RFC/ADR y se detiene si bloquea.

## 7) Estados y evidencia
- Estados permitidos en `docs/status/status.md`:
  NOT_STARTED, IN_PROGRESS, READY_FOR_VALIDATION, DONE, BLOCKED
- DONE/APROBADO SOLO cuando el usuario ejecuta validación local y la evidencia queda en LOG.

## 8) Flujo por tandas (docs) y por pistas (sprints)
- Tandas: documentos base en incrementos (T1, T2, …).
- Sprints: se trabajan en 2 conversaciones separadas:
  - BACK: solo sprints backend
  - FRONT: solo sprints frontend
- Ambos basados en `docs/**` (no en memoria del chat).

## 9) Formato obligatorio de respuesta de Codex
Codex debe responder SIEMPRE:

[CODEX-REPORT]
Project Lock:
* project_name: ...
* repo_url: ...
* branch: ...
* commit: ...

Work Done:
* ...

Files:
* Added:
* Modified:
* Deleted:

Commands Executed:
* ...

Status:
* git status: clean/dirty
* Next step: ...

<!-- EOF -->

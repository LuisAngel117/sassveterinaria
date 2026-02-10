# 00 — Índice canónico (fuente de verdad)

Este repositorio trabaja **local-first** y con **gobernanza por docs**:
- La conversación NO es fuente de verdad.
- La fuente de verdad vive en `docs/**` y se aplica por tandas/sprints.
- Sprints son **inmutables**: cambios SOLO por RFC/ADR/CHANGELOG.

## 1) Orden de lectura obligatorio (para agentes y humanos)

**Antes de tocar código, SIEMPRE leer (mínimo):**
1. `docs/project-lock.md`
2. `AGENTS.md` (si existe)
3. `docs/00-indice.md` (este archivo)
4. `docs/state/state.md`
5. `docs/quality/definition-of-ready.md`
6. `docs/quality/definition-of-done.md`
7. Sprint actual `docs/sprints/<SPR-XXX>.md` (cuando existan)
8. `docs/status/status.md` + `docs/log/log.md`

## 2) Mapa de documentos (tabla canónica)

| Archivo | Propósito | ¿Siempre leer antes de codificar? |
|---|---|---|
| `docs/project-lock.md` | Candado de identidad del proyecto | Sí |
| `docs/state/state.md` | Snapshot de estado + próximo sprint recomendado | Sí |
| `docs/quality/definition-of-ready.md` | Gate DoR (bloquea sprints) | Sí |
| `docs/quality/definition-of-done.md` | Gate DoD (bloquea READY_FOR_VALIDATION) | Sí |
| `docs/01-brief.md` | Contexto de negocio + glosario | Sí |
| `docs/02-brd.md` | Requisitos (BRD-REQ-###) | Sí |
| `docs/03-arquitectura.md` | Arquitectura, datos, seguridad, convenciones API | Sí |
| `docs/04-convenciones.md` | Normas duras (naming, git, scripts, casing) | Sí |
| `docs/05-seguridad.md` | Modelo auth/2FA/rate-limit/auditoría | Sí |
| `docs/06-dominio-parte-a.md` | Dominio: entidades y reglas (A) | Sí |
| `docs/06-dominio-parte-b.md` | Dominio: entidades y reglas (B) | Sí |
| `docs/07-ux-ui-parte-a.md` | UX/UI vendible (A) | Sí |
| `docs/07-ux-ui-parte-b.md` | UX/UI vendible (B) | Sí |
| `docs/08-runbook.md` | Operación local (setup/run/troubleshoot) | Sí |
| `docs/09-stage-release.md` | Qué cambia al ir online/stage (futuro) | No (solo si aplica) |
| `docs/10-permisos.md` | Matriz rol→permisos + acciones sensibles | Sí |
| `docs/11-entrega.md` | Checklist vendible local + RC | Sí |
| `docs/traceability/rtm.md` | Trazabilidad Req→Sprint→Evidencia | Sí |
| `docs/handoff/handoff-back-to-front.md` | Contrato real backend→frontend (cuando exista) | Front: Sí |
| `docs/status/status.md` | Tabla de estados de tandas/sprints | Sí |
| `docs/log/log.md` | Bitácora append-only (evidencia) | Sí |
| `docs/changelog.md` | Cambios (append-only) | Sí |
| `docs/rfcs/*.md` | Propuestas de cambio | Cuando aplique |
| `docs/decisions/*.md` | Decisiones (ADRs) | Cuando aplique |
| `docs/sprints/*.md` | Planes maestros y sprints (inmutables) | Cuando existan |

## 3) Casing Linux-friendly (evitar duplicados)

- Todo en `docs/**` va en minúsculas y rutas canónicas como arriba.
- Si aparece histórico como `LOG.md`, se interpreta como `docs/log/log.md`. **No crear duplicados**.

## 4) Estados y evidencia

- `READY_FOR_VALIDATION`: Codex terminó implementación/doc y dejó evidencia/placeholder en LOG/STATUS.
- `DONE`: SOLO usuario tras validar local y pegar outputs en `docs/log/log.md`.

## 5) Cómo se hacen cambios

- No se editan sprints ya emitidos.
- Cambios solo por:
  - `docs/rfcs/RFC-00xx-*.md`
  - `docs/decisions/ADR-00xx-*.md`
  - `docs/changelog.md`

## 6) Multi-conversación (BACK/FRONT)

- BACK y FRONT se trabajan en conversaciones separadas.
- Ambas deben basarse solo en `docs/**`.
- FRONT no debe “inventar” contratos: requiere `docs/handoff/handoff-back-to-front.md` cuando backend esté listo.

## 7) EOF obligatorio

- Todo `.md` bajo `docs/**` termina EXACTO con `<!-- EOF -->`.
- Script verificador: `scripts/verify/verify-docs-eof.ps1`.

<!-- EOF -->

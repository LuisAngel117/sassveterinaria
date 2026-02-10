# 00 — Índice y orden de lectura (fuente de verdad)

## Propósito

Este repo se gobierna por documentación dentro de `docs/**`. El chat NO es fuente de verdad.

## Reglas clave (resumen)

- Todo `.md` bajo `docs/**` termina EXACTO con `<!-- EOF -->`.
- Sprints (cuando existan) son inmutables: cambios SOLO por RFC/ADR/CHANGELOG.
- Evidencia:
  - `docs/log/log.md` append-only
  - `docs/status/status.md` tabla de control
- Estados:
  - `READY_FOR_VALIDATION` = implementado pero falta validación local del usuario
  - `DONE/APROBADO` = SOLO cuando el usuario ejecuta validación local y deja evidencia en LOG

## Orden de lectura obligatorio para agentes (Codex)

1) `docs/project-lock.md` (identidad / anti-mezcla)
2) `AGENTS.md` (conducta del agente)
3) `docs/state/state.md` (snapshot actual)
4) `docs/quality/definition-of-ready.md` (DoR — gate de inicio)
5) `docs/quality/definition-of-done.md` (DoD — gate de cierre)
6) `docs/01-brief.md`
7) `docs/02-brd.md` (contrato funcional + IDs BRD-REQ-###)
8) `docs/03-arquitectura.md`
9) `docs/04-convenciones.md`
10) `docs/05-seguridad.md`
11) `docs/06-dominio-parte-a.md` + `docs/06-dominio-parte-b.md`
12) `docs/07-ux-ui-parte-a.md` + `docs/07-ux-ui-parte-b.md`
13) `docs/08-runbook.md`
14) `docs/09-stage-release.md`
15) `docs/10-permisos.md`
16) `docs/11-entrega.md`
17) `docs/traceability/rtm.md`
18) `docs/sprints/spr-master-back.md` y/o `docs/sprints/spr-master-front.md` (según pista)
19) Sprint actual `docs/sprints/SPR-XXX.md` (cuando exista)
20) `docs/status/status.md`
21) `docs/log/log.md`
22) RFC/ADR referenciados por el sprint (si aplica)

## Nombres canónicos linux-friendly

- `docs/log/log.md` (NO crear `LOG.md`)
- `docs/status/status.md` (NO crear `STATUS.md`)
- `docs/changelog.md` (NO crear `CHANGELOG.md`)
- Sin espacios, casing consistente.

## Trabajo por tandas (docs)

- Cada tanda (T1, T2, T3, …) aplica cambios de docs (y solo docs + archivos explícitos como `AGENTS.md`).
- Codex aplica la tanda con scope estricto y un único commit.

## Pistas BACK y FRONT

- Los sprints detallados se generan en conversaciones separadas:
  - Conversación BACK: solo sprints backend
  - Conversación FRONT: solo sprints frontend
- Ambas conversaciones usan estos `.md` como fuente de verdad.

<!-- EOF -->

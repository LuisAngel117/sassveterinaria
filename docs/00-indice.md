# 00 - Indice canonico (fuente de verdad)

## 1) Regla central
- Todo el contexto del proyecto vive en `docs/**`.
- La conversacion no es fuente de verdad.
- Cambios de plan solo por `RFC/ADR/changelog`.
- Naming Linux strict: minusculas, sin espacios, case consistente.

## 2) Orden de lectura obligatorio (agentes)
1) `docs/project-lock.md`
2) `docs/00-indice.md`
3) `docs/state/state.md`
4) `docs/status/status.md`
5) `docs/log/log.md`
6) `docs/quality/definition-of-ready.md`
7) `docs/quality/definition-of-done.md`
8) `docs/traceability/rtm.md`
9) `docs/decisions/*.md`
10) `docs/rfcs/*.md` (si aplica)
11) `docs/sprints/spr-master-back.md` o `docs/sprints/spr-master-front.md`
12) Sprint actual `docs/sprints/<SPR-XXX>.md`

## 3) Mapa canonico
| Archivo | Proposito | Lectura previa |
|---|---|---|
| docs/project-lock.md | Candado de identidad del proyecto | Obligatoria |
| docs/01-brief.md | Contexto de negocio y vision | Obligatoria |
| docs/02-brd.md | Requisitos funcionales y no funcionales | Obligatoria |
| docs/03-arquitectura.md | Arquitectura, contratos y pruebas | Obligatoria |
| docs/04-convenciones.md | Normas duras de ejecucion | Obligatoria |
| docs/05-seguridad.md | Auth, permisos, auditoria y hardening | Obligatoria |
| docs/06-dominio-parte-a.md | Modelo de datos base | Obligatoria |
| docs/06-dominio-parte-b.md | Estados, auditoria y seeds | Obligatoria |
| docs/07-ux-ui-parte-a.md | Mapa UX y navegacion por rol | Front |
| docs/07-ux-ui-parte-b.md | Flujos criticos y reglas visuales | Front |
| docs/08-runbook.md | Operacion local y troubleshooting | Obligatoria |
| docs/09-stage-release.md | Reglas para stage/online (futuro) | Referencia |
| docs/10-permisos.md | Matriz estable rol -> permisos | Obligatoria |
| docs/11-entrega.md | Checklist de entrega vendible local | Obligatoria |
| docs/changelog.md | Historial de cambios | Obligatoria |
| docs/status/status.md | Estado de tandas/sprints | Obligatoria |
| docs/log/log.md | Bitacora append-only con evidencia | Obligatoria |
| docs/traceability/rtm.md | Trazabilidad req -> sprint -> evidencia | Obligatoria |

## 4) Reglas de modificacion
- `docs/sprints/*.md` son inmutables luego de aprobacion.
- `docs/log/log.md` es append-only.
- `docs/changelog.md` es append-only.
- `docs/status/status.md` no borra historico; solo agrega/actualiza filas.
- `DONE` solo lo marca el usuario tras validacion local con evidencia.

## 5) Regla EOF
- Todo `.md` bajo `docs/**` termina exacto con `<!-- EOF -->`.
- Verificar con: `pwsh -File scripts/verify/verify-docs-eof.ps1`
- Preflight completo: `pwsh -File scripts/verify/preflight.ps1`

## 6) Estado actual
- T1: bootstrap de gobernanza y scripts completado.
- T2: docs base detallados listos en estado `READY_FOR_VALIDATION`.
- Implementacion funcional queda para sprints BACK/FRONT.

## 7) Formato obligatorio de respuesta de Codex

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

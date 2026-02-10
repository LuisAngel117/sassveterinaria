# AGENTS — Conducta obligatoria para agentes (Codex u otros)

Este repo opera con “docs como fuente de verdad”.
La conversación NO es fuente de verdad.

## Orden mínimo de lectura (antes de tocar cualquier cosa)
1) docs/project-lock.md
2) docs/00-indice.md
3) docs/state/state.md
4) docs/status/status.md
5) docs/log/log.md
6) docs/quality/definition-of-ready.md y docs/quality/definition-of-done.md
7) (si aplica) docs/sprints/spr-master-back.md o spr-master-front.md
8) Sprint actual (docs/sprints/<SPR-XXX>.md)
9) RFC/ADR relacionados (si el sprint los referencia)

## Reglas duras (no negociables)
- NO inventar requisitos, rutas, scripts, usuarios git, comandos ni “supuestos”.
- NO tocar archivos fuera del scope (lista explícita del sprint o tanda).
- Sprints son INMUTABLES: no editarlos. Cambios solo por RFC/ADR/CHANGELOG.
- LOG es append-only: nunca reescribir entradas.
- DONE/APROBADO solo lo marca el usuario tras validación local con evidencia (en LOG).
- Mantener consistencia Linux: nombres sin espacios, case consistente.

## Quality gates (DoR/DoD)
- Antes de iniciar un sprint: validar DoR (docs/quality/definition-of-ready.md). Si falla → BLOCKED y detener.
- Antes de dejar READY_FOR_VALIDATION: validar DoD (docs/quality/definition-of-done.md). Si falla → DoD FAIL y detener o dejar IN_PROGRESS según el sprint.

## Scripts “verdad” (cuando existan)
- scripts/verify/preflight.ps1 (siempre antes y después de tandas/sprints de docs)
- scripts/verify/verify-docs-eof.ps1 (parte del preflight)

## Formato de reporte (Codex)
Codex debe responder con [CODEX-REPORT] (ver plantilla en docs/00-indice.md).

<!-- Nota: EOF obligatorio solo para docs/** (AGENTS.md no es obligatorio). -->

# State Snapshot (entrada única)

## Resumen actual
- Proyecto: SaaSVeterinaria (demo local-first).
- Estado docs: T2 emitida (brief+brd+arquitectura+ux+runbook+masters).
- Sprints: aún no ejecutados; solo planes maestros listos para aceptación “tal cual”.

## Decisiones cerradas (ADRs)
- Stack: `docs/decisions/adr-0001-stack.md`
- Arquitectura modular: `docs/decisions/adr-0002-arquitectura.md`
- Scoping branch (header+claim): `docs/decisions/adr-0003-tenancy-scoping.md`
- Seguridad JWT+2FA: `docs/decisions/adr-0004-seguridad-auth.md`
- Auditoría: `docs/decisions/adr-0005-auditoria.md`
- UX principios: `docs/decisions/adr-0006-ux-principios.md`
- Walking skeleton: `docs/decisions/adr-0007-walking-skeleton.md`

## Requerimientos cerrados vs pendientes
- Cerrados: ninguno (aún no hay implementación).
- Pendientes: todos los BRD-REQ-### en `docs/02-brd.md`.

## Estado tandas/sprints
- Ver `docs/status/status.md`.

## Próximo paso recomendado
1) Aceptar plan maestro BACK “tal cual” en conversación BACK.
2) Generar sprints backend detallados (SPR-B001 en adelante).
3) Ejecutar backend hasta tener handoff real.
4) Iniciar conversación FRONT con handoff, aceptar master FRONT y generar sprints FRONT.

## Riesgos/bloqueos
- Riesgo: inventar estructura repo/paths/comandos. Mitigación: DoR y Runbook marcan TBD donde no hay certeza.
- Riesgo: piezas sueltas. Mitigación: DoD exige vertical slice.

<!-- EOF -->

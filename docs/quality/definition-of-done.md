# Definition of Done (DoD)

Regla:
- Un item no puede quedar `READY_FOR_VALIDATION` si falla DoD.
- Si falla, registrar `DoD FAIL` en log y mantener `IN_PROGRESS` o `BLOCKED`.

## Checklist DoD verificable
1. AC del item cumplidos y documentados.
2. Build/test aplicables en verde (o `N/A` justificado).
3. Smoke test ejecutado con evidencia en `docs/log/log.md`.
4. Documentacion actualizada:
   - `docs/status/status.md`
   - `docs/log/log.md`
   - `docs/changelog.md`
   - `docs/traceability/rtm.md`
   - `docs/state/state.md`
5. Seguridad validada en lo tocado (auth, permisos, scoping, auditoria).
6. `pwsh -File scripts/verify/verify-docs-eof.ps1` en verde.
7. `git status --porcelain` limpio al final.

## Cierre formal
`DONE` solo lo marca el usuario tras validacion local con evidencia.

<!-- EOF -->

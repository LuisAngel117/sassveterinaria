# Definition of Done (DoD) — Gate para READY_FOR_VALIDATION

Regla:
- Un sprint NO puede quedar READY_FOR_VALIDATION si falla DoD.
- Si falla: registrar “DoD FAIL” en LOG + dejar IN_PROGRESS o BLOCKED según corresponda.

## Checklist DoD (verificable)
1) AC del sprint completados (checklist).
2) Incremento funcional e integrado (vertical slice cuando aplique).
3) Repositorio compila y/o build pasa:
   - backend: ./mvnw test (si aplica)
   - frontend: npm run build (si aplica)
   - si N/A, justificación explícita en el sprint.
4) Smoke test definido en el sprint:
   - comandos ejecutables
   - evidencia registrada en docs/log/log.md (output o placeholder “PEGAR OUTPUT AQUÍ”).
5) Docs actualizadas:
   - docs/status/status.md actualizado (READY_FOR_VALIDATION + commit hash)
   - docs/log/log.md con entrada append-only
   - docs/traceability/rtm.md actualizado si tocó/cerró BRD-REQ-###
   - docs/state/state.md actualizado (snapshot + next step)
6) Seguridad y auditoría:
   - acciones sensibles auditadas (si aplica)
   - no hay bypass de scoping
7) Repo limpio al final:
   - git status --porcelain vacío

<!-- EOF -->

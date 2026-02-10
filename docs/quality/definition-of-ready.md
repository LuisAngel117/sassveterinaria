# Definition of Ready (DoR) — Gate para iniciar sprints

Regla:
- Un sprint NO puede pasar a IN_PROGRESS si falla DoR.
- Si falla: marcar BLOCKED en docs/status/status.md + registrar en docs/log/log.md.

## Checklist DoR (verificable)
1) Sprint existe en docs/sprints/ y está referenciado por el master correspondiente.
2) El sprint declara:
   - Objetivo claro
   - Incluye/Excluye
   - AC verificables
   - Smoke test manual con comandos exactos
3) BRD/RTM:
   - El sprint declara qué BRD-REQ-### busca cerrar (o “N/A” con justificación si es infra).
4) Decisiones críticas cerradas (si aplica al sprint):
   - scoping branch (header + claim) definido
   - roles/permisos definidos para las acciones tocadas
   - reglas de negocio duras definidas (no-solape, estados, etc.)
5) Datos:
   - migraciones definidas (o N/A)
   - seeds demo definidos (o N/A)
6) Seguridad:
   - auth requerida o N/A explícito
   - acciones sensibles con reason required listadas si aplica
7) Runbook:
   - instrucciones para correr local lo tocado (o TBD justificado solo en sprints muy tempranos)
8) “No inventar”:
   - si falta info para implementar, el sprint debe indicar “crear RFC y detener”.

<!-- EOF -->

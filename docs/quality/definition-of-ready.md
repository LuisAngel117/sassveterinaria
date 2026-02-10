# Definition of Ready (DoR)

Regla:
- Un sprint no inicia en `IN_PROGRESS` si falla DoR.
- Si falla, marcar `BLOCKED` en status y registrar causa en log.

## Checklist DoR verificable
1. Sprint existe y esta referenciado en el master correspondiente.
2. Objetivo del sprint es claro y medible.
3. Incluye/excluye y AC verificables declarados.
4. Requisitos BRD (`BRD-REQ-###`) mapeados en RTM.
5. Dependencias tecnicas identificadas (DB, auth, UI, datos).
6. Riesgos y bloqueos conocidos registrados.
7. Smoke test manual definido con comandos reales.
8. Impacto en seguridad/permisos explicitado.
9. Plan de trazabilidad definido (`status/log/changelog/rtm/state`).
10. Si falta informacion critica, el sprint indica "crear RFC y detener".

## Resultado esperado
- DoR OK -> sprint puede iniciar.
- DoR FAIL -> no iniciar implementacion.

<!-- EOF -->

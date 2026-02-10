# Definition of Ready (DoR)

Un sprint NO puede iniciar si falla cualquiera de estos ítems (o debe crearse RFC/ADR de excepción).

## Checklist DoR (verificable)

### Documentación y trazabilidad
- [ ] Existe el sprint `docs/sprints/SPR-XXX.md` con alcance cerrado (incluye/excluye).
- [ ] El sprint declara los `BRD-REQ-###` que pretende tocar/cerrar (o “N/A” justificado).
- [ ] Existe mapeo inicial en `docs/traceability/rtm.md` (req → sprint) para esos IDs.
- [ ] No hay contradicciones abiertas con BRD/Arquitectura/Seguridad; si hay, existe RFC y el sprint está BLOCKED.

### Entradas técnicas
- [ ] Backend/Front scripts “verdad” definidos o marcados N/A con razón.
- [ ] Entorno local descrito en `docs/08-runbook.md` para lo que el sprint toca.

### Datos / scoping / seguridad
- [ ] Si toca endpoints branch-scoped: regla de `X-Branch-Id` está clara.
- [ ] Permisos requeridos están definidos en `docs/10-permisos.md`.
- [ ] Acciones sensibles incluyen “reason required” si aplica.

### QA / smoke
- [ ] El sprint define smoke manual (comandos + evidencia en LOG).
- [ ] AC es checklist verificable (no abstracto).

## Regla de bloqueo

- Si falla un ítem crítico: el sprint debe quedar `BLOCKED` en `docs/status/status.md` con nota “DoR FAIL”.

<!-- EOF -->

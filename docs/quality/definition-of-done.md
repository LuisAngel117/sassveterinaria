# Definition of Done (DoD)

Un sprint NO puede quedar `READY_FOR_VALIDATION` si falla cualquiera de estos ítems.

## Checklist DoD (verificable)

### Incremento funcional e integrado
- [ ] Entrega un incremento usable (vertical slice) o una base “Foundation” con justificación y desbloqueo real del siguiente sprint.
- [ ] No deja “piezas sueltas” (UI sin API, endpoints sin flujo, reglas sin validación).

### Calidad técnica mínima
- [ ] Compila/build (backend y/o frontend según aplique).
- [ ] Tests “verdad” del sprint pasan, o N/A justificado.
- [ ] Errores y validaciones consistentes (Problem Details si aplica).
- [ ] Permisos aplicados (no “TODO permitido”).

### Operación y evidencia
- [ ] `docs/log/log.md` tiene entrada append-only con comandos + outputs (o placeholders explícitos).
- [ ] `docs/status/status.md` actualizado a `READY_FOR_VALIDATION` con evidencia (hash commit).
- [ ] `docs/traceability/rtm.md` actualizado (req → sprint → evidencia → verificación → estado).
- [ ] `docs/state/state.md` actualizado (snapshot + next sprint recomendado).

### Seguridad / auditoría (si aplica)
- [ ] Acciones sensibles piden `reason` y guardan before/after.
- [ ] Auditoría registra actor, timestamp, branch, entidad afectada.

## Regla

- Si falla cualquier ítem: NO marcar `READY_FOR_VALIDATION`. Mantener `IN_PROGRESS` o `BLOCKED` y registrar “DoD FAIL” en LOG.

<!-- EOF -->

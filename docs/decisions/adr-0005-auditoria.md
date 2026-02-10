# ADR-0005 — Auditoría con before/after en acciones sensibles

## Contexto
Trazabilidad y control son parte de “vendible”.

## Decisión
- Registrar eventos clave y acciones sensibles con:
  - actor, branch, acción, entidad, reason, before_json, after_json, timestamp

## Consecuencias
- Coste de almacenamiento (retención demo 90 días).
- UI debe mostrar auditoría (al menos para admin).

## Alternativas descartadas
- Logs de texto sin estructura (difícil evidencia).

## Fecha
2026-02-10

<!-- EOF -->

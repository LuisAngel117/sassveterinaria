# ADR-0005 - Auditoria

## Contexto
El valor vendible depende de trazabilidad de acciones sensibles (clinicas, financieras y de inventario).

## Decision
- Auditar acciones sensibles con evento estructurado.
- Guardar actor, entidad, accion, timestamp, reason y before/after.
- Retencion inicial de 90 dias en entorno demo local.

## Consecuencias
- Mejora investigacion y soporte frente a incidentes.
- Incrementa volumen de datos y necesidad de filtros/reportes.

## Alternativas descartadas
- Logging plano sin estructura: insuficiente para trazabilidad forense.
- Auditoria parcial sin before/after: pierde contexto de cambio.

## Fecha
2026-02-10

<!-- EOF -->

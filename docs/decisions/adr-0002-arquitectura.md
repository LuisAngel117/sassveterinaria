# ADR-0002 — Arquitectura monolito modular

## Contexto
Evitar piezas sueltas y facilitar trazabilidad.

## Decisión
Usar monolito modular en backend con módulos por dominio (auth/agenda/crm/clinical/billing/inventory/reports/audit).

## Consecuencias
- Menos complejidad de despliegue.
- Mejor consistencia transaccional.

## Alternativas descartadas
- Microservicios (sobrecoste para demo).

## Fecha
2026-02-10

<!-- EOF -->

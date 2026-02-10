# ADR-0007 - Walking Skeleton

## Contexto
Existe riesgo alto de construir piezas aisladas sin integracion real entre auth, agenda, clinica e inventario.

## Decision
- Priorizar un walking skeleton temprano:
  - login + scoping de sucursal,
  - crear cita,
  - registrar SOAP,
  - emitir factura,
  - reflejar auditoria.
- Expandir funcionalidades solo despues de validar ese flujo.

## Consecuencias
- Detecta integraciones rotas al inicio.
- Reduce retrabajo de arquitectura.
- Obliga a priorizar vertical slices sobre backlog horizontal.

## Alternativas descartadas
- Construir primero capas completas por separado sin flujo integrado.
- Posponer validacion end-to-end para etapas tardias.

## Fecha
2026-02-10

<!-- EOF -->

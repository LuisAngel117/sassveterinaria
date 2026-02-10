# ADR-0003 - Tenancy Scoping

- Fecha: 2026-02-10
- Estado: vigente

## Contexto
El producto maneja informacion sensible de multiples clinicas.

## Decision
Aplicar scoping de tenant obligatorio en persistencia, consultas y autorizacion.

## Consecuencias
- Reduce riesgo de fuga de datos entre clinicas.
- Incrementa validaciones necesarias en todos los modulos.

<!-- EOF -->
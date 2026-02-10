# ADR-0002 - Arquitectura

## Contexto
El dominio mezcla agenda, clinica, facturacion e inventario; si no se separan modulos y capas, la complejidad crece rapidamente.

## Decision
- Adoptar monolito modular en backend.
- Separar capas: dominio, aplicacion, infraestructura, API.
- Limitar acoplamiento entre modulos por contratos explicitos.

## Consecuencias
- Mejor mantenibilidad y pruebas por modulo.
- Mayor disciplina en diseno y fronteras.
- Facilita evolucion a servicios separados en etapas futuras si fuera necesario.

## Alternativas descartadas
- Microservicios tempranos: sobrecosto operativo sin necesidad en V1.
- Monolito anemico sin fronteras: rapido al inicio pero fragil a mediano plazo.

## Fecha
2026-02-10

<!-- EOF -->

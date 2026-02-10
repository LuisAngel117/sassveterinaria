# ADR-0001 - Stack

## Contexto
Se requiere una base tecnica mantenible para una demo local-first/offline-first con backend fuerte en reglas de negocio y frontend web moderno.

## Decision
- Backend: Java 21 + Spring Boot 3.x.
- Base de datos: PostgreSQL 17.
- Migraciones: Flyway (version compatible con Postgres 17).
- Frontend: Next.js + TypeScript + Tailwind + shadcn/ui.
- Contrato API: OpenAPI + errores RFC 7807.

## Consecuencias
- Estandar robusto y conocido para evolucion futura.
- Curva inicial mayor que stacks minimalistas.
- Requiere disciplina de versionado y migraciones.

## Alternativas descartadas
- Node.js full-stack monolitico: mas rapido al inicio, menor separacion de capas para reglas complejas.
- PHP/Laravel: valida, pero menos alineada al objetivo tecnico del repositorio.

## Fecha
2026-02-10

<!-- EOF -->

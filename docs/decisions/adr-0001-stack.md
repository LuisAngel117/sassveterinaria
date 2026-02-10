# ADR-0001 — Stack

## Contexto
- Proyecto demo local-first/offline-first.
- Backend Java + DB Postgres + frontend Next.js.
- Migraciones con Flyway.

## Decisión
- Backend: Java 21 + Spring Boot (3.x)
- DB: PostgreSQL 17
- Migraciones: Flyway (usar versión reciente con soporte Postgres 17)
  - Referencias:
    - https://documentation.red-gate.com/fd/postgresql-database-277579325.html
    - https://www.postgresql.org/about/news/flyway-community-drift-check-released-2970/
- Frontend: Next.js (TypeScript) + Tailwind + shadcn/ui
- API docs: OpenAPI/Swagger

## Consecuencias
TBD

## Alternativas descartadas
TBD

## Fecha
2026-02-10

<!-- EOF -->

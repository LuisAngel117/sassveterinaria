# ADR-0001 — Stack técnico

## Contexto
Se necesita demo local-first vendible para portafolio, con backend robusto y frontend moderno.

## Decisión
- Backend: Java 21 + Spring Boot + Spring Security + JPA
- DB: Postgres 17
- Migraciones: Flyway (recomendado 10.20.1+)
- API: OpenAPI/Swagger
- Frontend: Next.js (TypeScript) + Tailwind + shadcn/ui
- Zona horaria: America/Guayaquil
- UI: Español
- Código: Inglés

## Consecuencias
- Local-first real: Postgres instalable local, sin Docker obligatorio al inicio.
- Compatibilidad Flyway/Postgres debe verificarse por versión.

## Alternativas descartadas
- Docker-first desde inicio (a futuro, no ahora).

## Fecha
2026-02-10

<!-- EOF -->

# 03 — Arquitectura

## 1) Resumen

- Backend: monolito modular (Spring Boot) con módulos por dominio (agenda, clientes, clínica, facturación, inventario, reportes, seguridad, auditoría).
- Frontend: Next.js (TypeScript) + Tailwind + shadcn/ui.
- DB: Postgres 17.
- Migraciones: Flyway.
- Objetivo: demo local-first (sin dependencias externas obligatorias).

## 2) Diagrama lógico (texto)

[Frontend Next.js]
  - Auth UI + selección de sucursal
  - Pantallas por módulo
  - Llamadas HTTP a API (JSON)

        |
        v

[Backend Spring Boot]
  - Seguridad (JWT + 2FA TOTP)
  - Scoping sucursal (X-Branch-Id + claims)
  - Dominios:
    - Agenda/Turnos
    - Clientes
    - Pacientes
    - Historia Clínica (SOAP)
    - Servicios
    - Facturación
    - Inventario
    - Reportes
  - Auditoría (eventos + before/after)

        |
        v

[Postgres 17]
  - Schema migrado por Flyway
  - Constraints para invariantes

## 3) Tenancy / Scoping (branch)

- No hay tenant.
- Hay sucursal (branch).
- Regla:
  - Endpoints branch-scoped EXIGEN `X-Branch-Id`.
  - El JWT incluye claims que indican qué branches puede usar el usuario.
  - Validación:
    - falta header → 400
    - header no permitido → 403
    - token inválido/ausente → 401

## 4) Seguridad

- JWT access + refresh.
- Rotación de refresh.
- Lockout por intentos fallidos (configurable; defaults definidos en BRD).
- 2FA TOTP para ADMIN/SUPERADMIN.
- Acciones sensibles requieren `reason` y auditan before/after.

## 5) Convenciones API

- Endpoints en inglés (consistencia técnica).
- UI en español.
- Errores: Problem Details (RFC 7807) como formato estándar.
- Paginación:
  - listados: `page`, `size`, `sort` (defaults definidos en implementación; documentar en OpenAPI).
- Fechas:
  - ISO-8601 con timezone (`America/Guayaquil` a nivel negocio).

## 6) Data y migraciones

- Flyway como herramienta de migración.
- Reglas:
  - Todas las tablas branch-scoped llevan `branch_id`.
  - Invariantes (no-solape, estados válidos) se respaldan con constraints + validación app.
- IDs:
  - UUID recomendado (o bigint autoincrement si se decide por ADR; por defecto UUID).

## 7) Money/decimales

- Montos: BigDecimal.
- Moneda: USD (implícito Ecuador).
- IVA configurable.

## 8) Archivos adjuntos (historia clínica)

- v1: adjuntos en atención (pdf/imagen).
- Almacenamiento: local filesystem o DB (decisión de implementación en sprint; documentar en ADR si impacta arquitectura).
- Límites: según BRD.

## 9) Estrategia de pruebas y smoke

- Unit tests donde aplique.
- Integration tests para repos y reglas críticas (no-solape, scope, facturación).
- Smoke scripts: PowerShell para flujo core.

## 10) Anti-desviación

- NO inventar: si falta decisión crítica → RFC/ADR y detener.
- Sprints inmutables.
- EOF en docs.

<!-- EOF -->

# 03 - Arquitectura

## Resumen
Arquitectura de referencia: monolito modular backend + frontend web desacoplado + PostgreSQL 17, con scoping por sucursal y auditoria transversal.

## Diagrama logico (texto)
1. Frontend (Next.js/TypeScript)
2. API Backend (Spring Boot, modulos por dominio)
3. Persistencia (PostgreSQL 17 + Flyway)
4. Cross-cutting: auth, permisos, auditoria, manejo de errores RFC 7807

Flujo general:
- Frontend autentica usuario.
- Backend entrega JWT con `branch_id` activo.
- En cada request branch-scoped, frontend envia `X-Branch-Id`.
- Backend valida header + claim + permisos antes de ejecutar caso de uso.
- Persistencia aplica reglas de integridad (incluyendo no-solape).

## Decisiones clave (ADR)
- Stack: `docs/decisions/adr-0001-stack.md`
- Arquitectura modular: `docs/decisions/adr-0002-arquitectura.md`
- Scoping de branch: `docs/decisions/adr-0003-tenancy-scoping.md`
- Seguridad/Auth: `docs/decisions/adr-0004-seguridad-auth.md`
- Auditoria: `docs/decisions/adr-0005-auditoria.md`
- UX principios: `docs/decisions/adr-0006-ux-principios.md`
- Walking skeleton: `docs/decisions/adr-0007-walking-skeleton.md`

## Tenancy y scoping (single-tenant + multi-branch)
- Tenant unico en V1.
- Aislamiento operativo por `branch_id`.
- Endpoints branch-scoped requieren `X-Branch-Id`.
- Regla estricta: si `X-Branch-Id` != `branch_id` del JWT, responder 403.
- Si falta header en endpoint branch-scoped, responder 400.

## Seguridad (auth, permisos, auditoria)
- Auth: JWT access 1h + refresh 7d (rotacion).
- 2FA TOTP obligatorio para `ADMIN` y `SUPERADMIN`.
- Autorizacion por permisos, no solo por rol.
- Auditoria con actor, entidad, before/after y reason required en acciones sensibles.

## Convenciones de API
- Rutas versionadas: `/api/v1/...`.
- Naming de recursos: sustantivos en plural (`/appointments`, `/patients`).
- Errores: Problem Details RFC 7807 (`type`, `title`, `status`, `detail`, `instance`).
- Fechas y horas en ISO-8601.

## Data y migraciones (PostgreSQL 17 + Flyway)
- DB principal: PostgreSQL 17.
- Migraciones incrementales e inmutables con Flyway.
- Constraint de no-solape de citas por sala en capa de persistencia.
- Semillas demo versionadas para flujo vendible local.

## Estrategia de pruebas
- Unit tests: reglas de dominio (no-solape, transiciones, permisos).
- Integration tests: repositorios, scoping, auth y errores.
- Smoke tests: flujo completo local en runbook.
- Validacion documental: `verify-docs-eof.ps1` y `preflight.ps1`.

## Anti-desviacion
- Si falta una decision critica, crear RFC/ADR y detener.
- No cambiar alcance de sprint sin actualizar RTM + changelog + status/log.
- No marcar `DONE` sin validacion local del usuario.

<!-- EOF -->

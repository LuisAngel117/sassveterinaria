# Changelog

## 2026-02-10 - T1
- Se crea estructura de gobernanza en `docs/`.
- Se agregan scripts `verify-docs-eof.ps1` y `preflight.ps1`.
- Se inicializan estado, log, calidad, RTM, ADRs, RFC y sprints.
- Estado de entrega: `READY_FOR_VALIDATION`.

## 2026-02-10 - T2
- Se completan docs base detallados: brief, BRD, arquitectura, dominio, UX/UI, runbook, permisos y masters.
- Se actualizan quality gates (DoR/DoD), RTM y state snapshot para iniciar sprints.
- Se mantienen reglas append-only en status/log/changelog.
- Estado de entrega: READY_FOR_VALIDATION.

## 2026-02-10
- T2: docs base detallados (brief+brd+arquitectura+ux+runbook+masters).  

## 2026-02-10 - SPR-B009
- Se implementa auditoria avanzada backend con consulta `GET /api/v1/audit/events` (AUDIT_READ, branch-scoped).
- Se agrega retencion configurable de auditoria (`app.audit.retention-days`, default 90) con purga programada diaria.
- Se auditan eventos de auth (`AUTH_LOGIN`, `AUTH_REFRESH`, `AUTH_LOGOUT`) y acciones core/sensibles con before/after.
- Se agrega migracion `V8__audit_advanced.sql` y pruebas de auditoria/purga (`AuditServiceIntegrationTests`).

## 2026-02-10 - SPR-B010
- Se implementa hardening de seguridad backend: 2FA TOTP (setup/enable/challenge/login-2fa), lockout configurable y rate limit 429.
- Se agrega migracion `V9__security_hardening.sql` para `totp_*`, `auth_login_attempt` y `auth_2fa_challenge`.
- Se agrega rate limit para login/refresh/reportes + header `Retry-After` en respuestas 429.
- Se agregan pruebas `SecurityHardeningIntegrationTests` y smoke script `scripts/smoke/spr-b010.ps1`.

## 2026-02-11 - SPR-B011
- Se extiende `DemoDataSeeder` con seed demo idempotente para room, citas y visita de ejemplo (ademas de usuarios/servicios/cliente/mascota).
- Se asegura credenciales demo fijas (`superadmin`, `admin`, `recepcion`, `veterinario`) con asignacion de branch por defecto.
- Se agrega smoke end-to-end `scripts/smoke/spr-b011.ps1` para flujo core: crear cita -> atender/cerrar -> facturar/pagar.
- Se actualiza runbook y trazabilidad (RTM/state/status/log) para dejar el sprint en `READY_FOR_VALIDATION`.

## 2026-02-10 - SPR-F001 (DoR FAIL)
- Se detecta bloqueo por ausencia de `FRONT_DIR` (no existe `package.json` de frontend en el repo).
- Se crea RFC de bloqueo `docs/rfcs/rfc-front-missing.md`.
- `SPR-F001` queda en `BLOCKED` en `docs/status/status.md`, sin implementacion de codigo.

## 2026-02-10 - Unblock FRONT bootstrap
- Se crea `frontend/` como root real frontend (Next.js + TypeScript + Tailwind, App Router).
- Se agrega `frontend/.env.example` con `NEXT_PUBLIC_API_BASE_URL`.
- Se actualiza runbook con comandos reales frontend (`npm install`, `npm run build`, `npm run dev`).
- Se levanta bloqueo tecnico de `SPR-F001` en status a `NOT_STARTED` para re-ejecucion del sprint.

## 2026-02-10 - SPR-F001
- Se implementa shell frontend minimo con rutas `/login`, `/select-branch` y `/`.
- Se integra contrato real de auth (`/api/v1/auth/login`, `/api/v1/me`, `/api/v1/auth/logout`) sin inventar campos.
- Se crea cliente API unico con soporte de `Authorization` y `X-Branch-Id` segun sesion.
- Se agrega session store local (tokens, permisos, branchId) y guardas de rutas.
- Se agrega helper de credenciales demo en login con fuente `docs/08-runbook.md`.
- Validacion tecnica: `npm run build` OK en `frontend/`.

## 2026-02-11 - SPR-F002
- Se implementa ruta frontend `/agenda` con vista semanal (lunes-domingo), filtros por sala/estado y carga real desde backend (`/api/v1/appointments`).
- Se habilita flujo de citas crear/editar con contratos reales (create + patch) y selector minimo de cliente/mascota usando CRM (`/api/v1/clients`, `/api/v1/clients/{id}/pets`).
- Se agregan acciones de transicion en UI: check-in, confirmar, iniciar atencion, cerrar y cancelar segun permisos de sesion (`APPT_*`).
- Se implementa manejo de conflicto de solape (`APPT_OVERLAP`) con reintento de sobre-cupo (`overbookReason`) mediante modal de motivo.
- Se integra UI minima de bloqueos manuales (`/api/v1/room-blocks`) para crear/listar cuando existe permiso `BRANCH_MANAGE`.
- Validacion tecnica frontend: `npm run lint` y `npm run build` OK; `npm run dev` ejecutado con timeout controlado.

## 2026-02-11 - SPR-F003
- Se implementa modulo CRM frontend con rutas `/clientes` (lista + busqueda + crear/editar cliente) y `/clientes/[clientId]` (ficha + mascotas).
- Se integra CRUD real de clientes y mascotas contra contratos de B003 (`/api/v1/clients`, `/api/v1/clients/{id}/pets`, `/api/v1/pets/{id}`) sin inventar endpoints.
- Se respeta matriz de permisos en UI (`CLIENT_READ/CREATE/UPDATE`, `PET_READ/CREATE/UPDATE`) para navegacion y acciones.
- Se agrega manejo de errores Problem Details con mapeo de validaciones de campo (`errors` / `fieldErrors`) en formularios.
- Se incorpora mensaje humano para conflicto de codigo interno duplicado (`PET_INTERNAL_CODE_CONFLICT`) y validaciones UI minimas de identificacion, email y peso.
- Validacion tecnica frontend: `npm run lint` y `npm run build` OK; `npm run dev` falla por lock activo de otra instancia (`.next/dev/lock`).
<!-- EOF -->

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
<!-- EOF -->

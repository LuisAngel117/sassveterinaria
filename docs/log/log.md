# LOG — Bitácora (append-only)

Formato por entrada:
- Fecha/hora: America/Guayaquil
- Item: T1 / SPR-XXX
- Qué se hizo (bullets)
- Comandos ejecutados (bullets)
- Output (pegar aquí)
- Resultado (READY_FOR_VALIDATION / FAIL)

---

## 2026-02-10T09:00:00-05:00
Item: T1
Qué se hizo:
- Bootstrap de estructura docs/scripts (gobernanza)
- Se creó project-lock + AGENTS + índice
- Se agregaron quality gates (DoR/DoD) + RTM + state snapshot
- Se agregó verificador EOF + preflight

Comandos ejecutados:
- pwsh -File scripts/verify/preflight.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T10:30:00-05:00
Item: T2
Que se hizo:
- Overwrite completo de docs base detallados (brief, BRD, arquitectura, dominio, UX/UI, runbook, permisos, ADR/RFC/sprints).
- Actualizacion controlada de project-lock sin alterar created_at.
- Actualizacion append-only de changelog y status.
- Verificacion final de EOF en docs.

Comandos ejecutados:
- pwsh -File scripts/verify/verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUI

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10 -05:00 (America/Guayaquil)
Tanda: T2
Qué se hizo:
- Se aplicaron docs base detallados (brief, brd con BRD-REQ-###, arquitectura, seguridad, dominio, ux/ui, runbook).
- Se generaron masters BACK/FRONT para aceptación “tal cual”.
Comandos ejecutados:
- scripts/verify/verify-docs-eof.ps1
Output:
- PEGAR OUTPUT AQUÍ
Resultado:
- READY_FOR_VALIDATION
## 2026-02-10T15:50:00-05:00
Item: SPR-B001
Qué se hizo:
- Se creó backend Spring Boot (`backend/`) con Maven wrapper y dependencias del sprint.
- Se implementó auth (`/api/v1/auth/login`, `/refresh`, `/logout`, `/api/v1/me`) con JWT access + refresh rotativo.
- Se implementó scoping `X-Branch-Id` contra claim `branch_id` para endpoints branch-scoped.
- Se implementó caso core mínimo de agenda: `POST/GET /api/v1/appointments`.
- Se agregaron migraciones Flyway y seed idempotente demo.
- Se creó smoke script `scripts/smoke/spr-b001.ps1`.
- Se actualizó runbook, status, RTM y state.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- ./mvnw test (en backend)
- pwsh -File scripts/verify/verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T16:24:36-05:00
Item: SPR-B002
Qué se hizo:
- Se implemento Agenda Core backend: rooms, services minimos, room blocks y extension de appointments.
- Se implementaron estados/transiciones (`RESERVED`, `CONFIRMED`, `IN_ATTENTION`, `CLOSED`, `CANCELLED`) y endpoint de `checkin` separado.
- Se implemento no-solape por sala (citas no canceladas + bloqueos), con sobre-cupo solo con permiso `APPT_OVERBOOK` + reason + auditoria.
- Se agregaron endpoints de agenda: rooms, services read-only, appointments (create/list/patch/acciones), room-blocks.
- Se agrego smoke script `scripts/smoke/spr-b002.ps1`.
- Se actualizaron runbook, status, RTM y state.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- ./mvnw test (en backend)
- pwsh -ExecutionPolicy Bypass -File .\scripts\verify\verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T16:38:33-05:00
Item: SPR-B003
Qué se hizo:
- Se implemento modulo CRM backend de clientes y mascotas (CRUD parcial sin delete) branch-scoped.
- Se agrego busqueda de clientes por `fullName`, `phone` o `identification`.
- Se implemento invariante `pet.internalCode` unico por sucursal con conflicto 409 `PET_INTERNAL_CODE_CONFLICT`.
- Se implemento invariante v1 `1 mascota -> 1 propietario` via `pet.client_id` obligatorio y sin endpoint de multi-owner.
- Se agregaron migraciones Flyway para `client`/`pet` (`V3__crm_clients_pets.sql`) y seed demo minimo idempotente.
- Se agrego smoke script `scripts/smoke/spr-b003.ps1`.
- Se actualizo documentacion de dominio, runbook, status, RTM y state.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- ./mvnw test (en backend)
- pwsh -File scripts/verify/verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T16:53:24-05:00
Item: SPR-B004
Qué se hizo:
- Se implemento catalogo de servicios v1 con create/list/detail/update branch-scoped.
- Se agrego `priceBase` a `service` por migracion incremental y validaciones de servicio.
- Se aplicaron permisos `SERVICE_READ`, `SERVICE_CREATE`, `SERVICE_UPDATE` por rol.
- Se implemento accion sensible: cambio de `priceBase` requiere `reason` (min 10) y genera auditoria before/after.
- Se agrego seed demo de 3 servicios (idempotente) y smoke script `scripts/smoke/spr-b004.ps1`.
- Se actualizo runbook, status, RTM y state.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- ./mvnw test (en backend)
- pwsh -File scripts/verify/verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T17:17:39-05:00
Item: SPR-B005
Qué se hizo:
- Se implemento modulo clinico backend: visitas, plantillas SOAP, prescripciones y adjuntos con almacenamiento local.
- Se agrego migracion `V5__clinical_visits.sql` con tablas `visit`, `soap_template`, `prescription` y `visit_attachment`.
- Se aplicaron reglas de negocio: walk-in, bloqueo por visita cerrada, cierre/reapertura con reason y auditoria before/after.
- Se agregaron permisos `VISIT_*` en matriz por rol y propiedades configurables de adjuntos en `application.properties`.
- Se agrego smoke script `scripts/smoke/spr-b005.ps1`.
- Se actualizo evidencia documental en runbook, status, RTM y state.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- ./mvnw test (en backend)
- pwsh -ExecutionPolicy Bypass -File .\scripts\verify\verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T17:41:14-05:00
Item: SPR-B006
Qué se hizo:
- Se implemento modulo de facturacion backend (`billing/**`) con facturas asociadas a `visit_id`.
- Se agrego migracion `V6__billing_invoices.sql` (`tax_config`, `invoice`, `invoice_item`, `invoice_payment`, `invoice_counter`).
- Se implementaron endpoints de IVA (`GET/PUT /config/tax`), facturas, items, pagos, anulacion y export CSV/PDF.
- Se implemento export de indicaciones desde visita (`GET /api/v1/visits/{id}/instructions.pdf`).
- Se aplicaron reglas de negocio: descuentos por item/total, estado `PENDING/PAID/VOID`, pagos parciales/mixtos, bloqueo en VOID y auditoria en acciones sensibles.
- Se actualizaron permisos en matriz y seed de `tax_config` demo.
- Se agrego smoke script `scripts/smoke/spr-b006.ps1`.
- Se actualizo evidencia documental en dominio/runbook/status/RTM/state.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- ./mvnw test (en backend)
- pwsh -ExecutionPolicy Bypass -File .\scripts\verify\verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T18:21:40-05:00
Item: SPR-B007
Qué se hizo:
- Se implemento Inventario v1 backend con migracion `V7__inventory_core.sql` (`unit`, `product`, `product_stock`, `stock_movement`, `service_bom_item`).
- Se agregaron endpoints branch-scoped de productos/unidades/stock/movimientos/low-stock/BOM/consumo por visita.
- Se implemento costeo promedio ponderado en `IN`, snapshot de costo en salidas y lock pesimista de `product_stock`.
- Se integro validacion de stock en facturacion para items `PRODUCT` con bloqueo `insufficient_stock` y override auditado (`STOCK_OVERRIDE_INVOICE` + reason).
- Se actualizaron permisos por rol para inventario en `PermissionMatrix`.
- Se agregaron seeds demo de unidades, productos con stock inicial y BOM para servicio `Vacunacion`.
- Se agrego smoke script `scripts/smoke/spr-b007.ps1`.
- Se actualizo evidencia documental en runbook/status/RTM/state.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- ./mvnw test (en backend)
- pwsh -ExecutionPolicy Bypass -File .\\scripts\\verify\\verify-docs-eof.ps1

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T19:00:05-05:00
Item: SPR-B008
Qué se hizo:
- Se implementaron endpoints branch-scoped de reportes y dashboard (`/api/v1/reports/*`, `/api/v1/dashboard`).
- Se implementaron exportaciones CSV/PDF para reportes reutilizando OpenPDF existente en el repositorio.
- Se agregaron DTOs y servicio de reportes, validaciones (`from/to`, `from<=to`, `limit<=100`) y respuestas Problem Details (422) para errores de validacion.
- Se actualizaron permisos en matriz (`REPORT_READ`, `REPORT_EXPORT`) y repositorios para consultas agregadas por sucursal.
- Se agrego smoke script `scripts/smoke/spr-b008.ps1`.
- Se actualizo evidencia documental en runbook, status, RTM y state.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- cd backend; ./mvnw test
- pwsh -ExecutionPolicy Bypass -File .\scripts\verify\verify-docs-eof.ps1
- pwsh -File scripts/smoke/spr-b008.ps1 (N/A: requiere backend levantado + PostgreSQL local)

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T19:16:21-05:00
Item: SPR-B009
Qué se hizo:
- Se extendio auditoria backend con modelo avanzado (`actor_username`, `is_sensitive`, `ip`, `user_agent`) y migracion `V8__audit_advanced.sql`.
- Se implemento `AuditService` reutilizable con: eventos no-sensibles, eventos sensibles (`reason` + before/after), consulta paginada con filtros y purga por retencion configurable.
- Se agrego API protegida `GET /api/v1/audit/events` con permiso `AUDIT_READ`.
- Se implemento retencion (default 90 dias) via scheduler diario (`AuditRetentionScheduler`) y propiedades `app.audit.*`.
- Se integraron eventos de auditoria en auth (`AUTH_LOGIN`, `AUTH_REFRESH`, `AUTH_LOGOUT`) y modulos core (agenda, visitas, facturacion, inventario, config IVA).
- Se actualizaron permisos en `PermissionMatrix` para `AUDIT_READ`.
- Se agregaron pruebas `AuditServiceIntegrationTests` para creacion de evento, before/after sensible y purga de retencion.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- cd backend; ./mvnw test
- pwsh -ExecutionPolicy Bypass -File .\scripts\verify\verify-docs-eof.ps1
- ./mvnw spring-boot:run (N/A: no ejecutado en esta tanda)

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T19:40:05-05:00
Item: SPR-B010
Qué se hizo:
- Se implemento hardening de seguridad backend: 2FA TOTP (setup/enable/challenge/login-2fa), lockout configurable y rate limit para login/refresh/reportes.
- Se agrego persistencia para seguridad (`totp_*` en `app_user`, `auth_login_attempt`, `auth_2fa_challenge`) con migracion `V9__security_hardening.sql`.
- Se implemento cifrado de secreto TOTP, validacion RFC6238 y flujo de challenge temporal para ADMIN/SUPERADMIN.
- Se aplico lockout por intentos fallidos (default 4/15 -> lock 15) y auditoria de eventos auth (`AUTH_LOGIN_FAILED`, `AUTH_LOCKOUT`, `AUTH_UNLOCK`).
- Se agrego rate limit in-memory con respuesta 429 + `Retry-After` para login/refresh/reportes/export.
- Se agrego smoke script `scripts/smoke/spr-b010.ps1`.
- Se agregaron pruebas de hardening (`SecurityHardeningIntegrationTests`) para 2FA, lockout, rate limit y 403 por permisos.

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- cd backend; ./mvnw test
- pwsh -ExecutionPolicy Bypass -File .\scripts\verify\verify-docs-eof.ps1
- pwsh -File scripts/smoke/spr-b010.ps1 (N/A: no ejecutado en esta tanda)
- ./mvnw spring-boot:run (N/A: no ejecutado en esta tanda)

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T19:59:53-05:00
Item: SPR-B011
Qué se hizo:
- Se extendio `DemoDataSeeder` para seed demo idempotente con room, 2 citas y 1 visita de ejemplo, manteniendo usuarios/servicios/cliente/mascota demo.
- Se aseguraron credenciales demo fijas (`superadmin`, `admin`, `recepcion`, `veterinario`) y branch default.
- Se creo `scripts/smoke/spr-b011.ps1` para flujo core end-to-end (cita -> atencion -> cierre -> factura -> pago).
- Se actualizo runbook y trazabilidad documental (status, RTM, state, changelog).

Comandos ejecutados:
- git status --porcelain
- git config user.name; git config user.email
- git remote -v
- git rev-parse --abbrev-ref HEAD
- cd backend; ./mvnw test
- pwsh -ExecutionPolicy Bypass -File .\scripts\verify\verify-docs-eof.ps1
- ./mvnw spring-boot:run (N/A: no ejecutado en esta tanda; requiere backend interactivo con DB local para prueba manual)
- pwsh -File scripts/smoke/spr-b011.ps1 (N/A: no ejecutado en esta tanda; requiere backend levantado)

Output:
- PEGAR OUTPUT AQUÍ

Resultado:
- READY_FOR_VALIDATION

## 2026-02-10T20:39:39-05:00
Item: SPR-F001
Qué se hizo:
- Se ejecuto pre-check obligatorio: arbol limpio, `user.name`/`user.email` presentes, remote validado y branch reportada.
- Se completo la lectura obligatoria documental para el sprint FRONT.
- Se valido DoR y fallo por falta de `FRONT_DIR` (no existe `package.json` de frontend en el repo).
- Se creo RFC de bloqueo: `docs/rfcs/rfc-front-missing.md`.
- Se actualizo `docs/status/status.md` a `BLOCKED` con nota `DoR FAIL`.

Comandos ejecutados:
- `git status --porcelain`
- `git config user.name; git config user.email`
- `git remote -v`
- `git rev-parse --abbrev-ref HEAD`
- `Test-Path docs/sprints/spr-f001.md`
- `rg --files | rg \"package.json$\"`
- `Get-ChildItem -Path . -Recurse -Filter package.json -File`
- `pwsh -File scripts/verify/verify-docs-eof.ps1`
- `npm run build` (N/A por DoR FAIL)
- `npm run dev` (N/A por DoR FAIL)

Output:
- `git status --porcelain` => (vacio)
- `git config user.name` => `LuisSigsig`
- `git config user.email` => `luis.angel1995117@gmail.com`
- `git remote -v` => `origin https://github.com/LuisAngel117/sassveterinaria.git` (fetch/push)
- `git rev-parse --abbrev-ref HEAD` => `main`
- `Test-Path docs/sprints/spr-f001.md` => `True`
- `rg --files | rg \"package.json$\"` => sin resultados
- `Get-ChildItem ... package.json` => sin resultados
- `pwsh -File scripts/verify/verify-docs-eof.ps1` => `OK: Todos los docs .md bajo 'docs' terminan con '<!-- EOF -->'.`
- `npm run build` => N/A (no existe `FRONT_DIR`/`package.json`)
- `npm run dev` => N/A (no existe `FRONT_DIR`/`package.json`)

Resultado:
- BLOCKED (DoR FAIL: falta FRONT_DIR; ver RFC `docs/rfcs/rfc-front-missing.md`)
<!-- EOF -->



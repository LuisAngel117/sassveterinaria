# SPR-B004 — Servicios (catálogo + duración + precio base + reason/auditoría)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 1  
**Duración objetivo:** 45–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Completar el módulo **Servicios** para uso real:
  - CRUD v1 (crear/listar/detalle/actualizar).
  - Campos base: **name**, **durationMinutes**, **priceBase**.
  - Cambios sensibles de precio requieren **reason** + **auditoría before/after**.
- Asegurar permisos por rol (según `docs/10-permisos.md`):
  - ADMIN/SUPERADMIN: crear/actualizar.
  - RECEPCION/VETERINARIO: solo lectura.
- Cerrar estos requisitos:
  - **BRD-REQ-021** Catálogo servicios (nombre, duración, precioBase)
  - **BRD-REQ-022** Duración de cita se toma del servicio (si ya está en B002, aquí se consolida y se documenta; override queda fuera si no hay permiso definido)

## 2) Alcance

### Incluye

- DB/Flyway:
  - Asegurar tabla `service` branch-scoped con:
    - `name` (requerido)
    - `duration_minutes` (requerido, default 30)
    - `price_base` (requerido, BigDecimal, USD)
    - `is_active` (default true)
  - Índice de unicidad recomendado: `(branch_id, name)` (si aplica sin romper datos; si rompe, RFC y no forzar).
- API branch-scoped (requiere `X-Branch-Id` + permisos):
  - `POST /api/v1/services` (SERVICE_CREATE)
  - `GET /api/v1/services` (SERVICE_READ) — filtros opcionales: `active=true/false`, `q`
  - `GET /api/v1/services/{id}` (SERVICE_READ)
  - `PATCH /api/v1/services/{id}` (SERVICE_UPDATE)
    - Si cambia `priceBase` → **reason required** (min 10 chars) + auditoría before/after
- Validaciones + Problem Details:
  - 422: campos inválidos
  - 409: conflicto de unicidad (si se aplica)
  - 404: recurso fuera de branch (no revelar cross-branch)
- Seeds demo:
  - Al menos 3 servicios demo (solo si no existen), con duración y precio coherentes.
- Smoke script:
  - `scripts/smoke/spr-b004.ps1` (create/list/update con reason + permisos)
- Docs:
  - Actualizar `docs/08-runbook.md` con endpoints/uso y smoke B004.
  - RTM/state/status/log al cierre.

### Excluye

- Receta de consumo / BOM por servicio → **SPR-B007** (BRD-REQ-023).
- Override de duración de cita con permiso:
  - Si no existe permiso explícito en `docs/10-permisos.md`, NO inventar.
  - Se deja como “pendiente” (RFC solo si el BRD exige implementarlo ya).
- Versionado de precios o vigencias (tarifarios por fecha) → fuera de v1.

## 3) Pre-check (obligatorio para Codex)

- `git status` limpio (si no, DETENER).
- `git config user.name` y `git config user.email` existen (si no, DETENER).
- `git remote -v` coincide con `docs/project-lock.md` (repo_url) (si no, DETENER).
- Rama actual: `git rev-parse --abbrev-ref HEAD`.
- Lectura obligatoria (en este orden):
  - `docs/project-lock.md`
  - `AGENTS.md` (si existe)
  - `docs/00-indice.md`
  - `docs/state/state.md`
  - `docs/quality/definition-of-ready.md`
  - `docs/quality/definition-of-done.md`
  - `docs/02-brd.md`
  - `docs/03-arquitectura.md`
  - `docs/04-convenciones.md`
  - `docs/05-seguridad.md`
  - `docs/06-dominio-parte-a.md` + `docs/06-dominio-parte-b.md`
  - `docs/08-runbook.md`
  - `docs/10-permisos.md`
  - `docs/traceability/rtm.md`
  - `docs/sprints/spr-master-back.md`
  - Este sprint: `docs/sprints/spr-b004.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
  - ADRs relevantes: `docs/decisions/adr-0002-arquitectura.md`, `adr-0004-seguridad-auth.md`, `adr-0005-auditoria.md`

## 4) Entregables

- DB / Migraciones:
  - Migración nueva (ej. `V4__services_catalog.sql` o según convención) para:
    - crear/alter `service` (incluye `price_base`)
    - constraints/índices mínimos
- Backend:
  - Endpoints `services` completos v1.
  - Validaciones + Problem Details.
  - Autorización por permisos `SERVICE_*`.
  - Reason required + auditoría before/after cuando cambia `priceBase`.
- Seeds demo:
  - 3 servicios mínimos (solo si no existen).
- Scripts:
  - `scripts/smoke/spr-b004.ps1`
- Docs:
  - `docs/08-runbook.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
  - `docs/traceability/rtm.md`
  - `docs/state/state.md`

## 5) Instrucciones de implementación (cerradas)

### 5.1 Migraciones (service)

Si `service` ya existe (por SPR-B002), **NO recrear**; usar `ALTER TABLE` y migración incremental.

Campos mínimos (alineado a BRD-REQ-021):
- `service`:
  - `id uuid pk`
  - `branch_id uuid not null fk -> branch.id`
  - `name varchar(120) not null`
  - `duration_minutes int not null default 30`
  - `price_base numeric(12,2) not null` (BigDecimal)
  - `is_active boolean not null default true`
  - `created_at timestamptz not null`

Índices:
- `idx_service_branch_active` (branch_id, is_active)
- (Opcional) unique `(branch_id, name)` si no rompe datos; si rompe, reportar y NO aplicar sin RFC.

### 5.2 Contratos API (JSON camelCase)

Create:
- `POST /api/v1/services`
  - request: `name`, `durationMinutes`, `priceBase`
  - response: service completo (incluye `id`, `isActive`)

List:
- `GET /api/v1/services?active=true&q=...`
  - `q` busca por `name` (ILIKE/contains)
  - default `active=true` (si se implementa default, documentar)

Detail:
- `GET /api/v1/services/{id}`

Update:
- `PATCH /api/v1/services/{id}`
  - campos editables: `name?`, `durationMinutes?`, `priceBase?`, `isActive?`
  - si `priceBase` cambia:
    - requerir `reason` (string, min 10)
    - registrar auditoría before/after
  - si NO cambia `priceBase`, `reason` no es obligatorio

Errores:
- 422 para validación de campos (`SERVICE_VALIDATION_ERROR`)
- 409 para conflicto de nombre si se implementa unique (`SERVICE_NAME_CONFLICT`)
- 404 si id no existe en el branch (`SERVICE_NOT_FOUND`)

### 5.3 Validaciones mínimas

- `name`: requerido, trim, min 3, max 120
- `durationMinutes`: requerido, min 5, max 480
- `priceBase`: requerido, >= 0.00 y <= 99999.99
- `reason` (si aplica): min 10 chars

### 5.4 Permisos / roles (usar matriz `docs/10-permisos.md`)

- `SERVICE_READ`: SUPERADMIN/ADMIN/RECEPCION/VETERINARIO (read)
- `SERVICE_CREATE`: SUPERADMIN/ADMIN
- `SERVICE_UPDATE`: SUPERADMIN/ADMIN
- Acción sensible:
  - `SERVICE_UPDATE` cuando cambia precioBase → reason required + auditoría (ADR-0005)

### 5.5 Seeds demo

- Crear servicios demo solo si no existen para la branch demo:
  - “Consulta general” (30 min, $20.00)
  - “Vacunación” (20 min, $15.00)
  - “Control post-operatorio” (30 min, $18.00)
- No imprimir precios/credenciales en logs más allá de lo necesario.

## 6) Criterios de aceptación (AC)

- [ ] Backend compila y tests pasan: `./mvnw test`.
- [ ] CRUD servicios:
  - [ ] ADMIN crea un servicio (200/201).
  - [ ] RECEPCION puede listar/detalle (200).
  - [ ] RECEPCION NO puede crear (403).
  - [ ] ADMIN puede actualizar `durationMinutes` (200).
- [ ] Cambio sensible:
  - [ ] Actualizar `priceBase` SIN `reason` falla (422 Problem Details).
  - [ ] Actualizar `priceBase` CON `reason` pasa (200) y crea auditoría before/after.
- [ ] `scripts/smoke/spr-b004.ps1` existe y cubre:
  - login admin → create service → login recepcion → list → admin update price w/ reason → verify list.
- [ ] Evidencia docs:
  - [ ] `docs/log/log.md` entrada append-only `SPR-B004`
  - [ ] `docs/status/status.md` fila `SPR-B004` en `READY_FOR_VALIDATION` con hash commit
  - [ ] `docs/traceability/rtm.md` actualizado para BRD-REQ-021/022
  - [ ] `docs/state/state.md` actualizado con next sprint recomendado (esperado: `SPR-B005`)

## 7) Smoke test manual (usuario)

1) Levantar backend:
- `cd backend`
- `./mvnw spring-boot:run`

2) Ejecutar smoke:
- `pwsh -File scripts/smoke/spr-b004.ps1`

3) Evidencia:
- Pegar output en `docs/log/log.md` en la entrada de `SPR-B004` (o dejar placeholder explícito).

## 8) Comandos verdad

- Backend:
  - `cd backend`
  - `./mvnw test`
  - `./mvnw spring-boot:run`
- Docs verify:
  - `pwsh -File scripts/verify/verify-docs-eof.ps1`

## 9) DoD

- Cumple `docs/quality/definition-of-done.md` (además de AC).
- `docs/status/status.md` queda en `READY_FOR_VALIDATION` (nunca DONE por Codex).
- `docs/log/log.md` append-only con comandos + outputs/placeholder.
- `docs/traceability/rtm.md` actualizado con evidencia y verificación.
- `docs/state/state.md` actualizado con snapshot y next sprint recomendado.

## 10) Si hay huecos/contradicciones

- Prohibido editar este sprint.
- Si aparece contradicción entre BRD y permisos/acciones sensibles:
  - Crear RFC en `docs/rfcs/` y actualizar `docs/changelog.md`.
  - Si bloquea: marcar `SPR-B004` como `BLOCKED`, registrar en LOG y DETENER.

<!-- EOF -->

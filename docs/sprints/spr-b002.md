# SPR-B002 — Agenda Core (no-solape por sala + estados + check-in + bloqueos + semana)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 1  
**Duración objetivo:** 45–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Convertir la agenda en “core usable”:
  - Citas con **estados** y transiciones válidas.
  - Regla dura de **no-solape por sala** (room) con excepción controlada (**sobre-cupo**) solo con permiso + auditoría.
  - **Check-in** separado del estado “en atención”.
  - **Bloqueos manuales** de agenda por sala (feriado/ocupación no clínica).
  - API usable para **vista semanal** (filtros por sala y estado).
- Cerrar estos requisitos:
  - **BRD-REQ-010** CRUD citas con estados (reservado/confirmado/en_atencion/cerrado/cancelado)
  - **BRD-REQ-011** Vista calendario semana con filtros (sala + estado)
  - **BRD-REQ-012** No-solape por sala (regla dura)
  - **BRD-REQ-013** Sobre-cupo solo con permiso + auditoría
  - **BRD-REQ-014** Check-in separado de “en atención”
  - **BRD-REQ-015** Bloqueos manuales (slot bloqueado)
  - **BRD-REQ-022** Duración se toma del servicio (v1); override opcional con permiso + auditoría (si se implementa)

## 2) Alcance

### Incluye

- Extender modelo de agenda (DB + dominio + API):
  - `room` (sala) branch-scoped.
  - `appointment` con:
    - `room_id` requerido
    - `status` (enum)
    - `checked_in_at` (timestamp nullable)
    - `is_overbook` (boolean) + `overbook_reason` (nullable) **o** registrar reason solo en auditoría (elegir una y documentar)
    - relación mínima a `service` para duración (ver nota BRD-REQ-022).
  - Bloqueos manuales branch-scoped por sala (tabla dedicada).
- Regla no-solape (hard):
  - Aplica a citas **NO canceladas** y a bloqueos manuales.
  - Overlap criterio: `new.starts_at < existing.ends_at AND new.ends_at > existing.starts_at`.
- Sobre-cupo:
  - Solo permitido con permiso `APPT_OVERBOOK` + `reason` (obligatorio) + auditoría before/after.
- Estados:
  - Mantener valores de estado como constantes en inglés en código/API (UI traducirá):
    - `RESERVED`, `CONFIRMED`, `IN_ATTENTION`, `CLOSED`, `CANCELLED`
  - Reglas de transición mínimas:
    - RESERVED → CONFIRMED → IN_ATTENTION → CLOSED
    - CANCELLED terminal (desde RESERVED/CONFIRMED; desde IN_ATTENTION requiere reason + permiso)
    - No permitir saltos inválidos (422 Problem Details).
- Check-in:
  - `POST /api/v1/appointments/{id}/checkin` marca `checkedInAt=now()` sin cambiar status.
- API (endpoints mínimos; todos branch-scoped requieren `X-Branch-Id` y permiso):
  - Rooms:
    - `POST /api/v1/rooms` (crear)
    - `GET /api/v1/rooms` (listar activas)
  - Servicios (solo para BRD-REQ-022, **sin CRUD completo**):
    - Crear tabla `service` mínima + seed mínimo (1–3 servicios demo)
    - Endpoint opcional (solo si se necesita para smoke): `GET /api/v1/services` (read-only)
  - Appointments:
    - `POST /api/v1/appointments` (crear)
    - `GET /api/v1/appointments` (listar por rango `from/to` + filtros `roomId/status`)
    - `PATCH /api/v1/appointments/{id}` (reprogramar sala/horas; valida no-solape)
    - `POST /api/v1/appointments/{id}/confirm`
    - `POST /api/v1/appointments/{id}/start` (pasa a IN_ATTENTION)
    - `POST /api/v1/appointments/{id}/close` (pasa a CLOSED)
    - `POST /api/v1/appointments/{id}/cancel` (requiere reason según regla)
  - Bloqueos:
    - `POST /api/v1/room-blocks` (crear bloqueo)
    - `GET /api/v1/room-blocks` (listar por rango + sala)
    - `DELETE /api/v1/room-blocks/{id}` (eliminar)
- Auditoría:
  - Para acciones sensibles (al menos):
    - sobre-cupo (`APPT_OVERBOOK`)
    - cancelación sensible (cuando ya estaba CONFIRMED o IN_ATTENTION)
  - Guardar before/after JSON según ADR-0005.

### Excluye

- CRUD completo de servicios (catálogo/duración/precio) → **SPR-B004**.
- Reglas avanzadas de agenda (multi-vet, buffers, plantillas complejas) → futuros sprints si aparecen en BRD.
- Reportes de agenda → **SPR-B008**.
- Hardening de seguridad (2FA/lockout/rate-limit/permisos finos exhaustivos) → **SPR-B010**.

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
  - Este sprint: `docs/sprints/spr-b002.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
  - ADRs relevantes: `docs/decisions/adr-0002-arquitectura.md`, `adr-0003-tenancy-scoping.md`, `adr-0004-seguridad-auth.md`, `adr-0005-auditoria.md`, `adr-0007-walking-skeleton.md`

## 4) Entregables

- DB / Migraciones (Flyway):
  - Crear tabla `room` (si no existe).
  - Crear tabla `room_block` (bloqueos manuales) branch-scoped.
  - Extender `appointment` con campos de agenda core:
    - `room_id` (FK)
    - `status`
    - `checked_in_at`
    - `is_overbook` (y opcional `overbook_reason`)
    - `service_id` (FK a `service`) si se implementa duración por servicio.
  - Crear tabla `service` mínima (solo para duración) + seed mínimo.
- Backend:
  - Endpoints rooms, appointments, room-blocks (y services read-only si se usa).
  - Validaciones + Problem Details consistentes.
  - Autorización por permisos del módulo Agenda.
  - Auditoría before/after para sobre-cupo y cancelación sensible.
- Scripts:
  - `scripts/smoke/spr-b002.ps1` (overlap + sobre-cupo + check-in + week list).
- Docs:
  - `docs/08-runbook.md` actualizado con smoke B002 y notas de agenda.
  - `docs/status/status.md`, `docs/log/log.md`, `docs/traceability/rtm.md`, `docs/state/state.md` actualizados.

## 5) Instrucciones de implementación (cerradas)

### 5.1 Migraciones

- Crear una migración nueva (ej: `V2__agenda_core.sql` o siguiendo convención existente) para:
  1) `room`
  2) `service` mínima (solo campos necesarios para duración)
  3) `room_block`
  4) `appointment` (alter/add columns + FKs)

Campos mínimos recomendados:

- `room`:
  - `id uuid pk`
  - `branch_id uuid not null fk -> branch.id`
  - `name varchar(80) not null`
  - `is_active boolean not null default true`
  - `created_at timestamptz not null`
- `service` (mínimo para duración; no expandir):
  - `id uuid pk`
  - `branch_id uuid not null fk -> branch.id`
  - `name varchar(120) not null`
  - `duration_minutes int not null`
  - `is_active boolean not null default true`
  - `created_at timestamptz not null`
- `room_block`:
  - `id uuid pk`
  - `branch_id uuid not null fk -> branch.id`
  - `room_id uuid not null fk -> room.id`
  - `starts_at timestamptz not null`
  - `ends_at timestamptz not null`
  - `reason varchar(255) not null`
  - `created_by uuid not null fk -> app_user.id`
  - `created_at timestamptz not null`
- `appointment` (si ya existe, alter):
  - `room_id uuid` (hacer required a nivel app; a nivel DB elegir:
    - A) `not null` si no rompe datos existentes, o
    - B) nullable con RFC si hay conflicto; NO inventar migración destructiva)
  - `service_id uuid` (igual criterio)
  - `status varchar(30) not null` (si ya existe, mantener)
  - `checked_in_at timestamptz null`
  - `is_overbook boolean not null default false`

Reglas:
- Mantener `ends_at > starts_at` validado en app (constraint DB opcional).
- No-solape se valida en app (DB constraint complejo se pospone).

### 5.2 No-solape por sala

Implementar en capa de dominio/servicio una función única reutilizable:

- Inputs: `branchId`, `roomId`, `startsAt`, `endsAt`, `excludeAppointmentId?`
- Verifica conflictos con:
  - citas NO canceladas en el mismo room
  - bloqueos `room_block` en el mismo room
- Si hay conflicto:
  - Si usuario tiene `APPT_OVERBOOK`:
    - exigir `reason`
    - marcar `isOverbook=true`
    - registrar auditoría (acción `APPT_OVERBOOK`) con before/after
  - Si no:
    - 422 Problem Details con `errorCode=APPT_OVERLAP` y detalle claro

### 5.3 Duración por servicio (BRD-REQ-022)

- En `POST /api/v1/appointments`:
  - Request debe incluir `serviceId` y `startsAt`.
  - El backend calcula `endsAt = startsAt + service.durationMinutes`.
  - Override (solo si se implementa en este sprint):
    - permitir `durationMinutesOverride` solo con permiso explícito (si NO existe permiso definido para esto, NO inventar uno; en ese caso, dejar override **NO implementado** y documentar en Excluye/Notas y en RTM como “parte pendiente” con RFC).
- Seed: crear al menos 1 service demo con duración 30–45 min (sin ampliar).

### 5.4 Estados y acciones

Endpoints de acción (mínimos):

- `POST /appointments/{id}/confirm`:
  - RESERVED → CONFIRMED
- `POST /appointments/{id}/start`:
  - CONFIRMED → IN_ATTENTION
- `POST /appointments/{id}/close`:
  - IN_ATTENTION → CLOSED
- `POST /appointments/{id}/cancel`:
  - Desde RESERVED: cancelar sin reason (opcional) o con reason (permitido)
  - Desde CONFIRMED o IN_ATTENTION: **reason required** y registrar auditoría before/after

Check-in:
- `POST /appointments/{id}/checkin`:
  - set `checkedInAt=now()`
  - permitido si status != CANCELLED/CLOSED
  - no cambia status

### 5.5 Permisos mínimos (usar `docs/10-permisos.md`)

- Rooms:
  - create: `BRANCH_MANAGE` o `APPT_UPDATE` (elegir una consistente; si hay duda, usar `BRANCH_MANAGE` para creación y `APPT_READ` para listado)
- Appointments:
  - list: `APPT_READ`
  - create: `APPT_CREATE`
  - patch/reprogram: `APPT_UPDATE`
  - checkin: `APPT_CHECKIN`
  - confirm/start/close: `APPT_START_VISIT` y/o `APPT_CLOSE` (según acción)
  - cancel: `APPT_CANCEL`
  - sobre-cupo: `APPT_OVERBOOK`
- Room blocks:
  - create/delete: `BRANCH_MANAGE` (o `APPT_UPDATE` si se decide; mantener consistente)

### 5.6 Vista semana

- `GET /api/v1/appointments` debe soportar:
  - `from` (ISO-8601)
  - `to` (ISO-8601)
  - filtros opcionales: `roomId`, `status`
- Esto debe ser suficiente para UI “semana”.
- (Opcional) Si se agrega default semana:
  - Si falta `from/to`, usar rango “hoy..hoy+7 días”, y documentarlo en OpenAPI/Runbook.

### 5.7 Errores (Problem Details)

- Validación negocio (solape, transición inválida, sala inexistente, service inexistente):
  - 422 `application/problem+json`
  - `errorCode` estable (`APPT_OVERLAP`, `APPT_INVALID_TRANSITION`, etc.)

## 6) Criterios de aceptación (AC)

- [ ] Backend compila y tests pasan: `./mvnw test`.
- [ ] No-solape:
  - [ ] Crear cita A en room X (ok).
  - [ ] Crear cita B que se cruza en el mismo room X (sin `APPT_OVERBOOK`) → 422 `APPT_OVERLAP`.
  - [ ] Con usuario con `APPT_OVERBOOK` y `reason` → permite y marca `isOverbook=true` + auditoría registrada.
- [ ] Estados:
  - [ ] Confirm: RESERVED → CONFIRMED
  - [ ] Start: CONFIRMED → IN_ATTENTION
  - [ ] Close: IN_ATTENTION → CLOSED
  - [ ] Transición inválida → 422 `APPT_INVALID_TRANSITION`
- [ ] Check-in:
  - [ ] `checkin` setea `checkedInAt` y NO cambia status.
- [ ] Bloqueos:
  - [ ] Crear `room_block` en un rango.
  - [ ] Intentar crear cita que se cruza con el bloqueo (sin sobre-cupo) → 422.
- [ ] Vista semana:
  - [ ] `GET /appointments?from=...&to=...` devuelve citas filtrables por `roomId` y `status`.
- [ ] Duración por servicio:
  - [ ] `POST /appointments` con `serviceId` calcula `endsAt` según `durationMinutes`.
- [ ] Auditoría:
  - [ ] Sobre-cupo registra evento con actor/branch/action/reason/before/after.
  - [ ] Cancelación sensible (desde CONFIRMED o IN_ATTENTION) exige reason y registra auditoría.
- [ ] `scripts/smoke/spr-b002.ps1` existe y cubre: login → crear room/service (si aplica) → crear cita → probar solape → probar sobre-cupo → check-in → week list.
- [ ] Evidencia docs:
  - [ ] `docs/log/log.md` entrada append-only `SPR-B002`
  - [ ] `docs/status/status.md` fila `SPR-B002` en `READY_FOR_VALIDATION` con hash commit
  - [ ] `docs/traceability/rtm.md` actualizado para BRD-REQ-010/011/012/013/014/015/022
  - [ ] `docs/state/state.md` actualizado con next sprint recomendado (esperado: `SPR-B003`)

## 7) Smoke test manual (usuario)

1) Levantar backend:
- `cd backend`
- `./mvnw spring-boot:run`

2) Ejecutar smoke:
- `pwsh -File scripts/smoke/spr-b002.ps1`

3) Evidencia:
- Pegar output en `docs/log/log.md` en la entrada de `SPR-B002` (o dejar placeholder explícito).

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
- Si aparece contradicción (ej: BRD-REQ-022 override requiere permiso no definido):
  - Crear RFC en `docs/rfcs/` para aclarar permiso/alcance,
  - Actualizar `docs/changelog.md`,
  - Si bloquea: marcar `SPR-B002` como `BLOCKED`, registrar en LOG y DETENER.

<!-- EOF -->

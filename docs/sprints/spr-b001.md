# SPR-B001 — Walking Skeleton (Auth + Scope + Base API + Smoke mínimo)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 1  
**Duración objetivo:** 45–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Entregar el “walking skeleton” backend: **auth + scope por sucursal + 1 caso core persistido (crear/listar cita simple)**, con **DB real (Postgres) y migraciones (Flyway)**.
- Establecer **contratos mínimos** (endpoints + errores Problem Details) y **smoke script** reproducible.
- Cerrar estos requisitos:
  - **BRD-REQ-001** Login access+refresh
  - **BRD-REQ-002** Refresh con rotación
  - **BRD-REQ-003** Logout revoca refresh
  - **BRD-REQ-007** Scope `X-Branch-Id` validado contra claims
  - **BRD-REQ-008** Respuestas 400/403/401 por scope
  - **BRD-REQ-057** Runbook local actualizado + scripts “verdad”

## 2) Alcance

### Incluye

- Backend Spring Boot (si no existe aún, crearlo en `backend/` como proyecto Maven).
- Flyway migrations mínimas para soportar:
  - `branch`, `app_user`, `user_branch`
  - almacenamiento de refresh tokens (tabla dedicada)
  - `appointment` **mínima** (campos core + branch_id; otras FK/constraints completas se difieren a SPR-B002/003)
- Auth:
  - `POST /api/v1/auth/login`
  - `POST /api/v1/auth/refresh` (rotación: refresh viejo invalida)
  - `POST /api/v1/auth/logout` (revoca refresh)
  - `GET /api/v1/me` (para inspección rápida de token/usuario/branch)
- Scoping sucursal:
  - Endpoints branch-scoped exigen `X-Branch-Id`
  - `X-Branch-Id` debe **coincidir** con claim del access token (`branch_id`) según ADR-0003
  - Errores:
    - falta header → **400**
    - header no permitido/mismatch → **403**
    - token inválido/ausente → **401**
- Base Problem Details (RFC 7807) para errores y validación.
- Seed mínimo demo:
  - 1 sucursal activa (“Sucursal Centro”)
  - usuarios demo mínimos con credenciales fijas (ver `docs/08-runbook.md`), asociados a la sucursal
- Script smoke mínimo para este sprint: `scripts/smoke/spr-b001.ps1`
- Actualización de `docs/08-runbook.md` para reflejar **comandos reales** del backend + smoke de B001.

### Excluye

- 2FA TOTP, lockout y rate limit (van en **SPR-B010**).
- Permisos finos completos por acción para TODO el sistema (foundation parcial ahora; hardening en SPR-B010).
- Reglas completas de agenda (no-solape por sala, sobre-cupo, estados completos, check-in) → **SPR-B002**.
- Entidades completas `client/pet/room` con invariantes y constraints fuertes → **SPR-B003** / **SPR-B002**.

## 3) Pre-check (obligatorio para Codex)

- `git status` debe estar limpio (si no, DETENER).
- `git config user.name` y `git config user.email` deben existir (si no, DETENER).
- `git remote -v` debe coincidir con `docs/project-lock.md` (repo_url) (si no, DETENER).
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
  - Este sprint: `docs/sprints/spr-b001.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
  - ADRs relevantes: `docs/decisions/adr-0002-arquitectura.md`, `adr-0003-tenancy-scoping.md`, `adr-0004-seguridad-auth.md`, `adr-0007-walking-skeleton.md`

## 4) Entregables

- Backend (código):
  - Proyecto Spring Boot en `backend/` (si no existe).
  - Auth + refresh rotativo + logout revocación.
  - Scope `X-Branch-Id` aplicado a endpoints branch-scoped.
  - Endpoints mínimos de agenda:
    - `POST /api/v1/appointments` (crear cita simple)
    - `GET /api/v1/appointments` (listar citas simples por branch; rango opcional)
  - Problem Details estándar para 400/401/403/422/500 (según aplique).
  - Migraciones Flyway iniciales.
  - Seed demo mínimo.
  - OpenAPI/Swagger habilitado (si se incluye dependencia; documentar URL exacta en Runbook).
- Scripts:
  - `scripts/smoke/spr-b001.ps1` (smoke mínimo del sprint).
- Docs (actualizar durante la ejecución del sprint):
  - `docs/08-runbook.md` (comandos reales + env vars reales + smoke B001).
  - `docs/status/status.md` (agregar/actualizar fila `SPR-B001`).
  - `docs/log/log.md` (entrada append-only de `SPR-B001` con comandos + outputs/placeholder).
  - `docs/traceability/rtm.md` (evidencia/estado de BRD-REQ tocados por B001).
  - `docs/state/state.md` (snapshot + siguiente sprint recomendado).

## 5) Instrucciones de implementación (cerradas)

### 5.1 Estructura backend (crear si no existe)

1) Si existe `backend/` con Spring Boot ya creado:
   - NO reestructurar.
   - Ajustar solo lo necesario para cumplir este sprint.
2) Si NO existe `backend/`:
   - Crear `backend/` como proyecto Maven Spring Boot (Java 21) con dependencias mínimas:
     - Web
     - Security
     - Validation
     - Data JPA
     - Flyway
     - Postgres driver
     - Actuator (para health)
     - OpenAPI (springdoc) si es viable sin inventar tooling externo
   - Paquete base recomendado: `com.sassveterinaria` (si el repo ya usa otro, NO cambiar; RFC si hay conflicto).

### 5.2 Migraciones (Flyway)

Crear `V1__init.sql` (o equivalente según convención actual del repo) que cree tablas mínimas:

- `branch` (según `docs/06-dominio-parte-a.md`):
  - `id uuid primary key`
  - `code varchar(20) unique not null`
  - `name varchar(120) not null`
  - `is_active boolean not null default true`
  - `created_at timestamptz not null`
- `app_user`:
  - `id uuid primary key`
  - `email varchar(160) unique not null` (nota: en demo puede almacenar “usuario” aunque no sea email real)
  - `full_name varchar(160) not null`
  - `password_hash varchar(255) not null`
  - `role_code varchar(30) not null`
  - `is_active boolean not null default true`
  - `locked_until timestamptz null` (aunque lockout se implemente en B010, el campo ya existe)
  - `created_at timestamptz not null`
- `user_branch`:
  - `user_id uuid not null` FK a `app_user(id)`
  - `branch_id uuid not null` FK a `branch(id)`
  - `is_default boolean not null default false`
  - PK compuesta (`user_id`,`branch_id`) o ID adicional (elegir una y mantenerla consistente)
- `auth_refresh_token` (tabla nueva para rotación/revocación):
  - `id uuid primary key`
  - `user_id uuid not null` FK a `app_user(id)`
  - `token_hash varchar(255) not null` (guardar hash, NO token plano)
  - `issued_at timestamptz not null`
  - `expires_at timestamptz not null`
  - `revoked_at timestamptz null`
  - `replaced_by uuid null` (opcional; FK a sí misma si se desea)
  - índices por `user_id`, `token_hash`
- `appointment` mínima:
  - `id uuid primary key`
  - `branch_id uuid not null` FK a `branch(id)`
  - `starts_at timestamptz not null`
  - `ends_at timestamptz not null`
  - `status varchar(30) not null`
  - `reason varchar(255) null`
  - `notes text null`
  - (campos futuros opcionales, permitir null por ahora): `room_id`, `client_id`, `pet_id`, `veterinarian_id` (uuid null)
  - `created_at timestamptz not null`

Reglas mínimas:
- `ends_at > starts_at` se valida en app (constraint DB opcional si es simple).
- NO implementar no-solape aquí.

### 5.3 Seed demo mínimo (sin inventar secretos)

Implementar un seeder idempotente (solo si DB está vacía) que cree:

- Branch:
  - `code=CENTRO`, `name="Sucursal Centro"`
- Usuarios (usando `docs/08-runbook.md` como fuente de credenciales):
  - `superadmin` / `SuperAdmin123!`
  - `admin` / `Admin123!`
  - `recepcion` / `Recepcion123!`
  - `veterinario` / `Veterinario123!`
- Asociar todos a la branch y marcar `is_default=true` para cada usuario en esa branch.

El seeder debe:
- Hashear contraseñas con `PasswordEncoder` (BCrypt recomendado).
- NO registrar passwords en logs.

### 5.4 JWT + Auth endpoints

Definir contratos JSON (camelCase) y mantenerlos estables:

- `POST /api/v1/auth/login`
  - Request:
    - `username` (string)  → mapear contra `app_user.email`
    - `password` (string)
  - Response (200):
    - `accessToken`
    - `refreshToken`
    - `expiresInSeconds`
    - `user`: `{ id, username, fullName, roleCode }`
    - `branch`: `{ id, code, name }` (branch “seleccionada” por default según `user_branch.is_default`)
  - Errores:
    - credenciales inválidas → 401 (Problem Details)
    - usuario inactivo → 403 o 401 (decidir una y documentar; mantener consistente)
- `POST /api/v1/auth/refresh`
  - Request: `{ refreshToken }`
  - Response: igual a login (nuevos tokens)
  - Regla: refresh viejo queda revocado/invalidado (rotación real)
- `POST /api/v1/auth/logout`
  - Request: `{ refreshToken }`
  - Regla: revoca refresh indicado
  - Response: 204 o 200 con body mínimo (decidir una y documentar)
- `GET /api/v1/me`
  - Requiere access token válido
  - Response: user + branch del token

JWT claims mínimos:
- `sub` = userId
- `role` = role_code
- `branch_id` = branchId seleccionado
- `perms` = lista de permisos (foundation; mínimo para endpoints de este sprint)

### 5.5 Permisos (foundation mínimo, sin ampliar)

Aplicar autorización mínima a endpoints de este sprint:

- `GET /api/v1/appointments` requiere `APPT_READ`
- `POST /api/v1/appointments` requiere `APPT_CREATE`

Mapeo mínimo sugerido (consistente con `docs/10-permisos.md`):
- RECEPCION: `BRANCH_SELECT`, `BRANCH_READ`, `APPT_READ`, `APPT_CREATE`
- VETERINARIO: `BRANCH_SELECT`, `BRANCH_READ`, `APPT_READ`
- ADMIN/SUPERADMIN: permitir ambos (y otros si se implementa wildcard, documentarlo)

### 5.6 Scoping `X-Branch-Id`

Implementar un gate para endpoints branch-scoped (para este sprint: appointments + cualquier otro `/api/v1/**` excepto `/api/v1/auth/**`):

- Si falta header `X-Branch-Id` → 400 (Problem Details)
- Si header != claim `branch_id` → 403 (Problem Details)
- Si no hay token o es inválido → 401

### 5.7 Problem Details (base)

Implementar respuesta estándar `application/problem+json` con mínimo:
- `type` (string URL-like)
- `title` (string)
- `status` (int)
- `detail` (string)
- `instance` (string, path)
Campos extra permitidos:
- `errorCode`
- `traceId`
- `errors` (para validación de campos)

## 6) Criterios de aceptación (AC)

- [ ] Backend compila y tests pasan: `./mvnw test` (en `backend/` o según estructura real).
- [ ] `GET /actuator/health` responde **UP** con backend corriendo.
- [ ] Login funciona:
  - [ ] Credenciales válidas de `recepcion` retornan access+refresh.
  - [ ] Credenciales inválidas retornan 401 en Problem Details.
- [ ] Refresh rota:
  - [ ] `refreshToken` válido emite token nuevo.
  - [ ] Reusar el refresh anterior falla (401/403 con Problem Details).
- [ ] Logout revoca:
  - [ ] Luego de logout, refresh revocado no puede refrescar.
- [ ] Scope funciona:
  - [ ] `POST /api/v1/appointments` sin `X-Branch-Id` → 400
  - [ ] con `X-Branch-Id` distinto al claim → 403
  - [ ] sin Authorization → 401
- [ ] Appointment mínima:
  - [ ] `POST /api/v1/appointments` crea una cita simple (persistida).
  - [ ] `GET /api/v1/appointments` lista e incluye la creada.
- [ ] Permisos mínimos aplicados:
  - [ ] `veterinario` NO puede crear cita (403).
- [ ] `scripts/smoke/spr-b001.ps1` existe y ejecuta el flujo “health → login → create appointment → list” (si falla, deja output claro).
- [ ] `docs/08-runbook.md` actualizado con:
  - [ ] comandos reales de levantar backend
  - [ ] env vars reales (o N/A con razón)
  - [ ] cómo correr smoke `spr-b001.ps1`
- [ ] Evidencia:
  - [ ] `docs/log/log.md` tiene entrada append-only `SPR-B001` con comandos y outputs/placeholder
  - [ ] `docs/status/status.md` tiene fila `SPR-B001` en `READY_FOR_VALIDATION` con hash commit
  - [ ] `docs/traceability/rtm.md` actualiza evidencia/estado para BRD-REQ-001/002/003/007/008/057
  - [ ] `docs/state/state.md` actualizado con snapshot y próximo sprint recomendado

## 7) Smoke test manual (usuario)

> Nota: ajustar host/puerto si tu config difiere. Por defecto: backend en `http://localhost:8080`.

1) Levantar backend
- PowerShell:
  - `cd backend`
  - `./mvnw spring-boot:run`

2) Ejecutar smoke script
- En otra terminal:
  - `pwsh -File scripts/smoke/spr-b001.ps1`

3) Evidencia
- Pegar output en `docs/log/log.md` en la entrada de `SPR-B001` (o dejar placeholder explícito si decides no pegar en chat).

## 8) Comandos verdad

- Backend:
  - `cd backend`
  - `./mvnw test`
  - `./mvnw spring-boot:run`
- Scripts verificación docs:
  - `pwsh -File scripts/verify/verify-docs-eof.ps1` (debe pasar)

## 9) DoD

- Cumple `docs/quality/definition-of-done.md` (además de AC).
- `docs/status/status.md` queda en `READY_FOR_VALIDATION` (nunca DONE por Codex).
- `docs/log/log.md` append-only con comandos + outputs/placeholder.
- `docs/traceability/rtm.md` actualizado con evidencia (commit) y estado.
- `docs/state/state.md` actualizado con:
  - estado real de `SPR-B001`
  - riesgos/bloqueos
  - siguiente sprint recomendado (esperado: `SPR-B002`).

## 10) Si hay huecos/contradicciones

- Prohibido editar este sprint.
- Si aparece un hueco que afecte arquitectura/contratos/seguridad:
  - Crear RFC en `docs/rfcs/` (y ADR si aplica),
  - Actualizar `docs/changelog.md`,
  - Si bloquea: marcar `SPR-B001` como `BLOCKED` con nota “DoR FAIL” o “bloqueado por RFC”, registrar en LOG y DETENER.

<!-- EOF -->

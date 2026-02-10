# SPR-B003 — Clientes y Mascotas (CRUD + búsqueda + invariantes)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 1  
**Duración objetivo:** 45–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Entregar un incremento usable del módulo **Clientes/Mascotas**:
  - CRUD de clientes (propietarios) y mascotas (pacientes).
  - Búsqueda de clientes por nombre/teléfono/identificación.
  - Invariantes v1:
    - **Código interno de mascota único por sucursal**.
    - **1 mascota → 1 propietario**.
- Cerrar estos requisitos:
  - **BRD-REQ-016** CRUD clientes
  - **BRD-REQ-017** Búsqueda clientes
  - **BRD-REQ-018** CRUD mascotas
  - **BRD-REQ-019** Código interno mascota único por sucursal
  - **BRD-REQ-020** 1 mascota = 1 propietario (v1)

## 2) Alcance

### Incluye

- DB + migraciones Flyway para `client` y `pet` (extendiendo si ya existen):
  - `client` con campos BRD (incluye `identification`, `notes`).
  - `pet` con `internal_code` (único por branch), y campos BRD.
- Endpoints branch-scoped (todos requieren `X-Branch-Id` + permisos):
  - Clientes:
    - `POST /api/v1/clients`
    - `GET /api/v1/clients` (lista + búsqueda con `q`)
    - `GET /api/v1/clients/{id}`
    - `PATCH /api/v1/clients/{id}`
  - Mascotas:
    - `POST /api/v1/clients/{clientId}/pets`
    - `GET /api/v1/clients/{clientId}/pets`
    - `GET /api/v1/pets/{id}`
    - `PATCH /api/v1/pets/{id}`
- Validaciones + Problem Details consistentes:
  - 400/401/403 por auth/scope (ya definido).
  - 404 para recursos fuera de branch (no filtrar existencia cross-branch).
  - 422 para validación de campos/reglas de negocio.
  - 409 para conflicto de unicidad (código interno duplicado).
- Seed demo mínimo (si aplica) para:
  - 1–2 clientes y 1–2 mascotas por la sucursal demo (solo si DB vacía).
- Script smoke: `scripts/smoke/spr-b003.ps1`.
- Actualizar `docs/08-runbook.md` con comandos reales + smoke B003.
- Mantener RTM + snapshot state al cerrar.

### Excluye

- Tags/consentimientos CRM (no están en BRD) → fuera de scope.
- Delete hard de clientes/mascotas (si se requiere, RFC futuro). En v1: no borrar o soft-delete futuro.
- Historia clínica / atenciones → SPR-B005.
- Facturación → SPR-B006.

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
  - Este sprint: `docs/sprints/spr-b003.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
  - ADRs relevantes: `docs/decisions/adr-0002-arquitectura.md`, `adr-0003-tenancy-scoping.md`, `adr-0004-seguridad-auth.md`

## 4) Entregables

- DB / Migraciones:
  - Migración nueva (ej. `V3__crm_clients_pets.sql` o según convención) para:
    - Crear/alter `client`
    - Crear/alter `pet`
    - Índice único `(branch_id, internal_code)` en `pet`
- Backend:
  - Controladores/servicios/repositorios para clientes y mascotas.
  - Validaciones + errores Problem Details.
  - Autorización por permisos:
    - `CLIENT_READ/CREATE/UPDATE`
    - `PET_READ/CREATE/UPDATE`
- Scripts:
  - `scripts/smoke/spr-b003.ps1`
- Docs (actualizar durante la ejecución del sprint):
  - `docs/06-dominio-parte-a.md` (si faltan campos en tablas `client`/`pet`, ajustar para alinearse con BRD)
  - `docs/08-runbook.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
  - `docs/traceability/rtm.md`
  - `docs/state/state.md`

## 5) Instrucciones de implementación (cerradas)

### 5.1 Migraciones (cliente)

Si `client` ya existe, hacer `ALTER TABLE` sin destruir datos.

Campos mínimos requeridos (alineados a BRD-REQ-016/017):

- `client`:
  - `id uuid pk`
  - `branch_id uuid not null fk -> branch.id`
  - `full_name varchar(160) not null`
  - `identification varchar(30) null`  (BRD: opcional)
  - `phone varchar(30) null`
  - `email varchar(160) null`
  - `address varchar(255) null`
  - `notes text null`
  - `created_at timestamptz not null`

Índices recomendados (mínimos, no sobre-optimizar):
- `idx_client_branch_full_name` (branch_id, full_name)
- `idx_client_branch_phone` (branch_id, phone)
- `idx_client_branch_identification` (branch_id, identification)

### 5.2 Migraciones (mascota)

- `pet`:
  - `id uuid pk`
  - `branch_id uuid not null fk -> branch.id`
  - `client_id uuid not null fk -> client.id`
  - `internal_code varchar(30) not null`
  - `name varchar(120) not null`
  - `species varchar(80) not null`
  - `breed varchar(120) null`
  - `sex varchar(20) null`
  - `birth_date date null`
  - `weight_kg numeric(6,2) null`
  - `neutered boolean null`
  - `alerts text null`
  - `history text null` (antecedentes)
  - `created_at timestamptz not null`

Unicidad:
- Unique index/constraint: `(branch_id, internal_code)`.

Regla v1:
- 1 mascota → 1 propietario queda garantizado por `client_id` (no modelar “many owners”).

### 5.3 Contratos API (JSON camelCase)

Clientes:
- Create `POST /clients`
  - request: `fullName`, `identification?`, `phone?`, `email?`, `address?`, `notes?`
  - response: cliente creado (incluye `id`)
- List/Search `GET /clients?q=...&page=&size=...`
  - si `q` existe: buscar por **fullName OR phone OR identification** (contains/ILIKE).
- Detail `GET /clients/{id}`
- Update `PATCH /clients/{id}`:
  - permitir actualizar campos editables; validar email si existe.

Mascotas:
- Create `POST /clients/{clientId}/pets`
  - request: `internalCode`, `name`, `species`, `breed?`, `sex?`, `birthDate?`, `weightKg?`, `neutered?`, `alerts?`, `history?`
- List `GET /clients/{clientId}/pets`
- Detail `GET /pets/{id}`
- Update `PATCH /pets/{id}`:
  - si se cambia `internalCode`, revalidar unicidad por branch.

### 5.4 Validaciones mínimas (sin inventar reglas clínicas)

- `fullName` requerido, trim, max 160.
- `email` si existe: formato email estándar (Bean Validation).
- `identification` si existe:
  - Validación mínima v1 (derivada de “cédula/RUC”):
    - debe ser solo dígitos y longitud 10 o 13.
  - Si esto resulta conflictivo con uso real, levantar RFC en el futuro (no bloquear este sprint).
- `pet.internalCode` requerido; trim; max 30.
- `pet.species` requerido; max 80.
- `weightKg` si existe: > 0 y <= 9999.99 (por tipo).

Errores:
- Campos inválidos → 422 Problem Details con `errors[]` por campo.
- `internalCode` duplicado (en el mismo branch) → 409 Problem Details con `errorCode=PET_INTERNAL_CODE_CONFLICT`.

### 5.5 Seguridad / Permisos / Scope

- Todos estos endpoints son branch-scoped:
  - exigir `X-Branch-Id` y validar contra claim (ya definido en ADR-0003).
- Permisos:
  - List/search/detail cliente: `CLIENT_READ`
  - Create cliente: `CLIENT_CREATE`
  - Update cliente: `CLIENT_UPDATE`
  - List/detail mascota: `PET_READ`
  - Create mascota: `PET_CREATE`
  - Update mascota: `PET_UPDATE`

### 5.6 Seed demo (opcional y mínimo)

- Si el proyecto ya tiene seeder de B001/B002:
  - Extenderlo para crear 1 cliente y 1 mascota demo, solo si no existen registros en branch demo.
- No imprimir datos sensibles en logs.

## 6) Criterios de aceptación (AC)

- [ ] Backend compila y tests pasan: `./mvnw test`.
- [ ] Clientes:
  - [ ] `POST /clients` crea cliente (persistido).
  - [ ] `GET /clients?q=...` encuentra por nombre.
  - [ ] `GET /clients?q=...` encuentra por phone o identification.
  - [ ] `PATCH /clients/{id}` actualiza campos.
- [ ] Mascotas:
  - [ ] `POST /clients/{clientId}/pets` crea mascota con `internalCode`.
  - [ ] `GET /clients/{clientId}/pets` lista y contiene la creada.
  - [ ] `PATCH /pets/{id}` actualiza datos.
- [ ] Invariantes:
  - [ ] Crear 2 mascotas con el mismo `internalCode` en el mismo branch falla con 409 `PET_INTERNAL_CODE_CONFLICT`.
  - [ ] No existe endpoint para asignar múltiples dueños (1 mascota → 1 propietario).
- [ ] Seguridad:
  - [ ] Endpoints sin `X-Branch-Id` → 400.
  - [ ] `X-Branch-Id` mismatch → 403.
- [ ] `scripts/smoke/spr-b003.ps1` existe y cubre:
  - login → crear cliente → buscar → crear mascota → duplicado internalCode (espera 409) → list.
- [ ] Docs/evidencia:
  - [ ] `docs/log/log.md` entrada append-only `SPR-B003`
  - [ ] `docs/status/status.md` fila `SPR-B003` en `READY_FOR_VALIDATION` con hash commit
  - [ ] `docs/traceability/rtm.md` actualizado para BRD-REQ-016..020 (evidencia + verificación)
  - [ ] `docs/state/state.md` actualizado con next sprint recomendado (esperado: `SPR-B004`)

## 7) Smoke test manual (usuario)

1) Levantar backend:
- `cd backend`
- `./mvnw spring-boot:run`

2) Ejecutar smoke:
- `pwsh -File scripts/smoke/spr-b003.ps1`

3) Evidencia:
- Pegar output en `docs/log/log.md` en la entrada de `SPR-B003` (o dejar placeholder explícito).

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
- Si se detecta contradicción entre BRD y dominio (ej: campos CRM “obligatorios” en `06-dominio-parte-b.md`):
  - Se alinea documentación de dominio a BRD (corrección de docs) como parte del sprint.
  - Solo si la contradicción cambia el BRD o seguridad/arquitectura: crear RFC/ADR y detener si bloquea.

<!-- EOF -->

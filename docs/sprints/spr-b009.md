# SPR-B009 — Auditoría avanzada (before/after + retención)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 3  
**Duración objetivo:** 60–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Implementar/terminar la **auditoría avanzada** según `docs/05-seguridad.md` y `docs/decisions/adr-0005-auditoria.md`.
- Cubrir **BRD-REQ-052, BRD-REQ-053, BRD-REQ-054** y el caso explícito de **auditar cambios de IVA** (BRD-REQ-032).
- Garantizar:
  - auditoría obligatoria en eventos clave (incluye auth y CRUD core),
  - **before/after** obligatorio en acciones sensibles,
  - **retención demo 90 días** (purgado/archivado simple),
  - endpoint(s) de consulta de auditoría (mínimo para ADMIN/SUPERADMIN) protegido por permiso.

## 2) Alcance

### Incluye

**BRD objetivo:** BRD-REQ-052, BRD-REQ-053, BRD-REQ-054 (y BRD-REQ-032 solo en lo referente a auditoría de IVA).

1) **Modelo y persistencia de auditoría**
   - Verificar si ya existe infraestructura de auditoría creada en sprints previos:
     - Si existe: **extenderla** para soportar before/after + retención.
     - Si no existe: **crear** la base mínima.
   - Definir una tabla (o equivalente) para eventos de auditoría que cumpla ADR-0005:
     - Campos mínimos requeridos (nombres exactos pueden variar por convención, pero deben existir):
       - `id`
       - `created_at` (timestamp)
       - `actor_user_id` (nullable si aplica)
       - `actor_username` (útil para demo)
       - `branch_id` (nullable para auth events sin contexto; obligatorio para branch-scoped)
       - `action` (string estable, en inglés, ej. `AUTH_LOGIN`, `APPT_CREATE`, `INVOICE_VOID`)
       - `entity_type` (string estable, en inglés, ej. `APPOINTMENT`, `INVOICE`)
       - `entity_id` (nullable si no aplica)
       - `is_sensitive` (bool)
       - `reason` (nullable; obligatorio en acciones sensibles)
       - `before_json` (nullable; obligatorio en acciones sensibles definidas abajo)
       - `after_json` (nullable; obligatorio en acciones sensibles definidas abajo)
       - `ip` / `user_agent` (si ya se captura; si no existe, añadir si es trivial)
   - Migración Flyway para crear/alterar lo necesario (idempotente por versión).

2) **API de consulta de auditoría (mínimo vendible)**
   - Endpoint(s) branch-scoped para consultar eventos (protegido por permiso **`AUDIT_READ`** de `docs/10-permisos.md`):
     - Debe exigir `X-Branch-Id` si la consulta es branch-scoped.
     - Debe permitir filtros básicos:
       - rango de fechas (from/to)
       - action
       - entity_type
       - entity_id
       - actor_user_id o actor_username (si existe)
     - Paginación (page/size) si el proyecto ya lo usa; si no, mínimo `limit`/`offset`.
   - Respuesta: lista ordenada por `created_at desc`.

3) **Cobertura de auditoría obligatoria (BRD-REQ-052)**
   - Implementar registro de auditoría para, como mínimo, estas categorías (si existen en el repo en el momento de ejecutar este sprint):
     - Auth:
       - login
       - refresh
       - logout
       - (si existen) intentos fallidos y lock/unlock
     - Cambios de roles/permisos:
       - asignación de roles
       - cambios de permisos
     - Agenda/Turnos:
       - create/update/cancel
       - overbook (si existe)
       - check-in/start/close (si existe)
     - Historia clínica / Atenciones:
       - create/update/close
       - reopen (si existe)
     - Facturación:
       - create/update
       - pay
       - void/anulación
       - export (si existe)
     - Inventario:
       - movimientos create (ingreso/egreso)
       - ajustes (si existe)
       - override por falta de stock (si existe)
     - Config IVA:
       - lectura y **cambio** (cambio obligatorio auditar con énfasis)
   - **Regla de bloqueo**: si una categoría existe en el repo (endpoints/servicios) pero no puede auditarse por falta de punto de integración claro, se debe crear RFC y el sprint queda **BLOCKED** (ver sección 10).

4) **Before/After + reason (BRD-REQ-053)**
   - Asegurar before/after (JSON) + reason (min 10 chars) en acciones sensibles mínimas definidas en `docs/05-seguridad.md`:
     - anular factura (`INVOICE_VOID`)
     - cambio de precio (servicio o ítem) (si existe)
     - reabrir historia (`VISIT_REOPEN`)
     - ajustes inventario manuales (`STOCK_ADJUST` si existe)
     - cambio de IVA/configuración fiscal (`CONFIG_TAX_UPDATE`)
     - sobre-cupo (`APPT_OVERBOOK`) si existe
   - Si una acción sensible no tiene `reason` actualmente:
     - agregar `reason` a request DTO y validarlo,
     - registrar auditoría con before/after.

5) **Retención 90 días (BRD-REQ-054)**
   - Implementar purgado/archivado simple:
     - Por defecto: **borrado** de eventos con `created_at < now - 90 días`.
     - La cantidad de días debe ser **configurable** (property), default 90.
   - Ejecutable como:
     - job programado (daily) **y/o**
     - comando manual interno (endpoint admin protegido) si ya existe patrón.
   - Debe existir evidencia técnica (test o log) de que el purgado funciona sin afectar eventos recientes.

### Excluye

- UI de auditoría (eso es FRONT).
- Export masivo de auditoría (CSV/PDF) salvo que ya exista infraestructura reutilizable.
- Integración con SIEM/servicios externos.
- Multi-tenant (no aplica).
- “Auditoría perfecta” en absolutamente todos los cambios internos; el foco es cobertura de eventos de negocio y sensibles.

## 3) Pre-check (obligatorio para Codex)

- `git status` limpio.
- `git config user.name` y `git config user.email` presentes.
- Rama actual reportada.
- Existe este archivo: `docs/sprints/spr-b009.md`.
- Lectura obligatoria (en este orden):
  - `AGENTS.md` (si existe)
  - `docs/project-lock.md`
  - `docs/00-indice.md`
  - `docs/state/state.md`
  - `docs/quality/definition-of-ready.md`
  - `docs/quality/definition-of-done.md`
  - `docs/02-brd.md`
  - `docs/03-arquitectura.md`
  - `docs/05-seguridad.md`
  - `docs/10-permisos.md`
  - ADRs: `docs/decisions/adr-0004-seguridad-auth.md`, `docs/decisions/adr-0005-auditoria.md`
  - `docs/traceability/rtm.md`
  - `docs/status/status.md`
  - `docs/log/log.md`
- Validar DoR:
  - Permiso `AUDIT_READ` existe en `docs/10-permisos.md`.
  - Acciones sensibles y regla `reason` están definidas (sí, en `docs/05-seguridad.md`).

## 4) Entregables

- Backend:
  - Migración Flyway para tabla/alter de auditoría.
  - Entidad/modelo + repositorio.
  - Servicio de auditoría (API interna simple y reutilizable).
  - Integraciones en puntos críticos (auth + acciones sensibles y CRUD core que existan).
  - Endpoint(s) de consulta de auditoría (protegido por `AUDIT_READ`).
  - Retención: job y/o comando manual.
  - Tests mínimos (unit/integration) para:
    - creación de evento audit,
    - before/after en acción sensible,
    - retención (purga de viejo, conserva reciente).
- Docs (al ejecutar el sprint):
  - `docs/status/status.md` actualizado a `READY_FOR_VALIDATION` (nunca DONE).
  - `docs/log/log.md` con entrada append-only (comandos + outputs/placeholders).
  - `docs/traceability/rtm.md` actualizado (BRD-REQ-052/053/054 → SPR-B009 + evidencia).
  - `docs/state/state.md` actualizado (snapshot + next sprint recomendado).
  - `docs/changelog.md` (si el repo lo está usando para registrar cambios).

## 5) Instrucciones de implementación (cerradas)

1) Persistencia
   - Crear/ajustar migración Flyway para `audit_event` (o nombre existente).
   - Asegurar índices básicos:
     - `created_at`
     - `branch_id, created_at`
     - `action, created_at`
     - `entity_type, entity_id`
   - Asegurar que JSON se guarde como `jsonb` si la convención del repo lo permite; si no, string.

2) Servicio de auditoría (reutilizable)
   - Implementar un `AuditService` (o nombre existente) con método(s) tipo:
     - `recordEvent(...)` para eventos no sensibles (sin before/after).
     - `recordSensitiveEvent(... beforeJson, afterJson, reason ...)` para sensibles.
   - Capturar actor:
     - user_id + username desde SecurityContext si autenticado
     - ip/user-agent si existe RequestContext; si no, dejar null y documentar.
   - Capturar branch:
     - si el request es branch-scoped, usar `X-Branch-Id`
     - si es auth event sin branch, permitir null

3) Integración obligatoria (mínimo)
   - Auth:
     - Al completar login exitoso: `AUTH_LOGIN`
     - Al refresh: `AUTH_REFRESH`
     - Al logout: `AUTH_LOGOUT`
   - Config IVA:
     - En `CONFIG_TAX_UPDATE`: registrar **SENSITIVE** con before/after + reason.
   - Acciones sensibles (si existen endpoints/servicios):
     - `INVOICE_VOID`, `VISIT_REOPEN`, `STOCK_ADJUST`, `APPT_OVERBOOK`, cambios de precio.
   - CRUD core:
     - Para cada módulo existente (agenda/visit/invoice/inventory), registrar al menos create/update/delete/cancel como evento no-sensible (salvo las marcadas sensibles).

4) API de consulta
   - Implementar controller branch-scoped:
     - `GET /api/v1/audit/events` (si el repo ya tiene prefijo/versionado; respetar convención real)
   - Requerir `AUDIT_READ`.
   - Exigir `X-Branch-Id` y filtrar por branch para eventos branch-scoped.
   - Permitir filtros y paginación mínima.

5) Retención
   - Implementar purga por default 90 días (configurable):
     - Opción A: scheduled job diario (recomendado).
     - Opción B: endpoint admin manual protegido (si existe módulo admin).
   - Agregar test que inserte 2 eventos (uno viejo, uno reciente) y valide que purga elimina solo el viejo.

6) Gobernanza y evidencia
   - Actualizar RTM y state snapshot al finalizar el sprint.
   - Si durante integración descubres falta de endpoints/módulos necesarios para cerrar BRD-REQ-052/053/054:
     - crear RFC y marcar el sprint como BLOCKED (ver sección 10).

## 6) Criterios de aceptación (AC)

- [ ] Existe migración Flyway para auditoría y aplica sin errores.
- [ ] Se registra auditoría en auth: login + refresh + logout (al menos exitosos).
- [ ] Se registra auditoría en **cambio IVA** como acción sensible:
  - [ ] requiere `reason` (min 10),
  - [ ] guarda before_json + after_json.
- [ ] Al menos **3** acciones sensibles (si existen en el repo al ejecutar) guardan before/after + reason correctamente:
  - [ ] `INVOICE_VOID` (si existe)
  - [ ] `VISIT_REOPEN` (si existe)
  - [ ] `STOCK_ADJUST` o `APPT_OVERBOOK` (si existe)
- [ ] Existe endpoint `GET .../audit/events` protegido por `AUDIT_READ`.
- [ ] El endpoint de auditoría:
  - [ ] exige `X-Branch-Id` (si es branch-scoped),
  - [ ] filtra por branch,
  - [ ] soporta filtros básicos (al menos action + rango fechas),
  - [ ] ordena por fecha desc.
- [ ] Retención:
  - [ ] existe mecanismo de purga 90 días configurable,
  - [ ] test demuestra que purga elimina eventos viejos y mantiene recientes.
- [ ] `./mvnw test` pasa.

## 7) Smoke test manual (usuario)

> Nota: el smoke asume que el backend puede correr local y que existe al menos un usuario ADMIN/SUPERADMIN y endpoints auth. Si falta, registrar bloqueo en LOG.

1) Levantar backend:
   - `./mvnw spring-boot:run`

2) Ejecutar flujo mínimo (manual o con curl/PowerShell):
   - Login (ADMIN) → obtener token.
   - Ejecutar una acción sensible disponible (preferible cambio IVA o anulación factura) con `reason`.
   - Consultar auditoría:
     - Llamar `GET .../audit/events` con `X-Branch-Id` válido y token.
     - Verificar que aparecen eventos:
       - `AUTH_LOGIN`
       - `CONFIG_TAX_UPDATE` (o la sensible que se ejecutó)

3) Evidencia:
   - Pegar outputs en `docs/log/log.md` en la entrada del sprint.

## 8) Comandos verdad

- Backend:
  - `./mvnw test`
  - `./mvnw spring-boot:run`
- Frontend: N/A

## 9) DoD

- Cumple `docs/quality/definition-of-done.md` además de los AC.
- `docs/status/status.md` queda en `READY_FOR_VALIDATION` (no DONE).
- `docs/log/log.md` tiene entrada append-only con comandos y outputs/placeholders.
- `docs/traceability/rtm.md` actualizado para BRD-REQ-052/053/054 con evidencia (commit).
- `docs/state/state.md` actualizado con snapshot y próximo sprint recomendado.

## 10) Si hay huecos/contradicciones

- Prohibido inventar endpoints, entidades o reglas.
- Si al ejecutar este sprint faltan piezas necesarias para cerrar BRD-REQ-052/053/054 (por ejemplo, aún no existe módulo de IVA o no existe ninguna acción sensible implementada):
  1) Crear RFC en `docs/rfcs/` describiendo el hueco (auditoría no puede integrarse aún).
  2) Actualizar `docs/changelog.md` (si aplica).
  3) Marcar `SPR-B009` como `BLOCKED` en `docs/status/status.md` con nota “DoR/Scope gap”.
  4) Registrar en `docs/log/log.md` (append-only) el motivo exacto del bloqueo.
  5) Detener sin dejar el repo roto.

<!-- EOF -->

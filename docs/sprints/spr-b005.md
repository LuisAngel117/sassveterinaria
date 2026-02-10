# SPR-B005 — Historia Clínica (Atención + SOAP + plantillas + adjuntos + cierre/reapertura)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Stage:** 1  
**Duración objetivo:** 45–90 min (referencial)  
**Tipo:** Backend  

## 1) Objetivo

- Entregar el módulo de **Historia Clínica** usable en local:
  - Crear “Atención/Visit” (walk-in o vinculada a cita).
  - Registrar SOAP mínimo por atención.
  - Plantillas SOAP por servicio (selección al crear atención).
  - Adjuntos (imagen/pdf) con límites BRD.
  - Cerrar atención (bloquea edición) y reabrir solo con permiso + reason + auditoría before/after.
  - Prescripción estructurada (medicamento/dosis/unidad/frecuencia/duración/vía/obs).
- Cerrar estos requisitos (según `docs/02-brd.md`):
  - **BRD-REQ-024** Atención sin cita (walk-in) + vínculo opcional
  - **BRD-REQ-025** SOAP mínimo por atención
  - **BRD-REQ-026** Plantillas SOAP por servicio
  - **BRD-REQ-027** Adjuntos: pdf/imagen; max 10MB; max 5 por atención (configurable)
  - **BRD-REQ-028** Cierre bloquea edición; reapertura con permiso+reason+auditoría
  - **BRD-REQ-029** Prescripción estructurada
  - **BRD-REQ-030** Export indicaciones (P1) → **NO se completa aquí** (ver Excluye; se deja RTM como pendiente)

## 2) Alcance

### Incluye

- DB/Flyway (branch-scoped):
  - Tabla `visit` (atención) con:
    - `pet_id` requerido
    - `service_id` requerido (para aplicar plantilla; walk-in igual elige un servicio)
    - `appointment_id` nullable (vínculo opcional)
    - `status` (`OPEN`/`CLOSED`)
    - campos SOAP mínimo (ver 5.1)
    - timestamps + `created_by`
  - Tabla `soap_template` por `service_id` (admin) con campos SOAP como texto (defaults para prellenar).
  - Tabla `visit_attachment` (metadata) + almacenamiento físico en filesystem usando `STORAGE_DIR` (runbook).
  - Tabla `prescription` (estructurada) asociada a `visit`.
- API branch-scoped (requiere `X-Branch-Id` + permisos):
  - Visitas:
    - `POST /api/v1/visits` (crear)
    - `GET /api/v1/visits/{id}` (detalle)
    - `GET /api/v1/pets/{petId}/visits` (historial por mascota; filtros opcionales `status`, `from`, `to`)
    - `PATCH /api/v1/visits/{id}` (editar SOAP mientras esté OPEN)
    - `POST /api/v1/visits/{id}/close`
    - `POST /api/v1/visits/{id}/reopen` (reason required + auditoría)
  - Plantillas SOAP:
    - `POST /api/v1/soap-templates` (crear)
    - `GET /api/v1/soap-templates?serviceId=...` (listar)
    - `GET /api/v1/soap-templates/{id}` (detalle)
    - `PATCH /api/v1/soap-templates/{id}` (editar)
  - Prescripciones:
    - `POST /api/v1/visits/{id}/prescriptions`
    - `GET /api/v1/visits/{id}/prescriptions`
    - `PATCH /api/v1/prescriptions/{id}` (editar mientras visita OPEN)
  - Adjuntos:
    - `POST /api/v1/visits/{id}/attachments` (multipart/form-data)
    - `GET /api/v1/visits/{id}/attachments` (lista metadata)
    - `GET /api/v1/attachments/{id}/download` (descargar)
    - `DELETE /api/v1/attachments/{id}` (solo si visita OPEN)
- Reglas de negocio:
  - Walk-in permitido (visit sin appointment).
  - Si `appointment_id` viene:
    - solo validar que pertenece al mismo branch y existe; **NO** cambiar estado de cita automáticamente en este sprint.
  - Cierre:
    - `CLOSED` bloquea `PATCH` de SOAP/prescripciones/adjuntos.
  - Reapertura:
    - requiere permiso `VISIT_REOPEN`
    - requiere `reason` (min 10 chars)
    - auditoría before/after (ADR-0005)
  - Adjuntos:
    - tipos permitidos: `application/pdf`, `image/jpeg`, `image/png`
    - max 10MB por archivo
    - max 5 adjuntos por visita (configurable con defaults; ver 5.3)
- Auditoría:
  - Reapertura (`VISIT_REOPEN`) debe auditar before/after + reason.
  - (Opcional si se toca) borrado de adjunto también es acción auditable (solo si ya existe convención; no inventar).

### Excluye

- **BRD-REQ-030** Export PDF/HTML de indicaciones → se difiere a **SPR-B006** (o sprint de reportes/export).
- Integración de visita con facturación (generar factura desde atención) → **SPR-B006**.
- Versionado complejo de plantillas (histórico) → fuera de v1.
- Almacenamiento en DB de binarios → fuera (se usa filesystem local-first).

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
  - `docs/08-runbook.md` (nota: `STORAGE_DIR`)
  - `docs/10-permisos.md`
  - `docs/decisions/adr-0005-auditoria.md`
  - `docs/traceability/rtm.md`
  - `docs/sprints/spr-master-back.md`
  - Este sprint: `docs/sprints/spr-b005.md`
  - `docs/status/status.md`
  - `docs/log/log.md`

## 4) Entregables

- DB / Migraciones:
  - Crear tablas: `visit`, `soap_template`, `visit_attachment`, `prescription`.
  - FKs y constraints mínimas.
- Backend:
  - Endpoints de visitas + plantillas + prescripciones + adjuntos.
  - Validaciones + Problem Details.
  - Permisos `VISIT_*` y control de cierre.
  - Auditoría de reapertura (before/after + reason).
- Scripts:
  - `scripts/smoke/spr-b005.ps1` (flujo clínico mínimo end-to-end).
- Docs:
  - `docs/08-runbook.md` (uso `STORAGE_DIR` + smoke B005)
  - `docs/status/status.md`
  - `docs/log/log.md`
  - `docs/traceability/rtm.md` (marcar BRD-REQ-024..029 como READY_FOR_VALIDATION; BRD-REQ-030 queda PLANNED/PENDING)
  - `docs/state/state.md` (snapshot + next sprint recomendado: `SPR-B006`)

## 5) Instrucciones de implementación (cerradas)

### 5.1 Modelo de datos (mínimo)

Migración nueva (ej. `V5__clinical_visits.sql` o convención existente).

`visit` (atención):
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `pet_id uuid not null fk -> pet.id`
- `service_id uuid not null fk -> service.id`
- `appointment_id uuid null fk -> appointment.id`
- `status varchar(20) not null` (`OPEN`/`CLOSED`)
- `checked_in_at` **NO** aplica aquí (ya existe en cita); no duplicar.
- SOAP mínimo (columnas):
  - `s_reason text not null` (motivo_consulta)
  - `s_anamnesis text not null`
  - `o_weight_kg numeric(6,2) null`
  - `o_temperature_c numeric(4,1) null`
  - `o_findings text null`
  - `a_diagnosis text null`
  - `a_severity varchar(30) null`
  - `p_treatment text null`
  - `p_instructions text null`
  - `p_followup_at date null`
- `created_by uuid not null fk -> app_user.id`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

Reglas:
- Al crear visita, `s_reason` y `s_anamnesis` deben existir (pueden ser “TBD” si el BRD lo permite; si no, exigir mínimo 1–3 chars).
- Cuando `status=CLOSED`:
  - bloquear updates (en app; constraint DB opcional).

`soap_template`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `service_id uuid not null fk -> service.id`
- `name varchar(120) not null`
- campos SOAP como texto (mismo set que en `visit`, pero nullable/plantilla):
  - `s_reason`, `s_anamnesis`, `o_findings`, `a_diagnosis`, `p_treatment`, `p_instructions`
  - (valores numéricos no se templatean por defecto)
- `is_active boolean not null default true`
- `created_at timestamptz not null`

`prescription`:
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `visit_id uuid not null fk -> visit.id`
- `medication varchar(160) not null`
- `dose varchar(60) not null`
- `unit varchar(30) not null`
- `frequency varchar(60) not null`
- `duration varchar(60) not null`
- `route varchar(60) not null`
- `notes text null`
- `created_at timestamptz not null`

`visit_attachment` (metadata):
- `id uuid pk`
- `branch_id uuid not null fk -> branch.id`
- `visit_id uuid not null fk -> visit.id`
- `original_filename varchar(255) not null`
- `content_type varchar(80) not null`
- `size_bytes bigint not null`
- `storage_path varchar(500) not null` (path relativo dentro de `STORAGE_DIR`)
- `created_by uuid not null fk -> app_user.id`
- `created_at timestamptz not null`

### 5.2 Reglas de edición / cierre

- `PATCH /visits/{id}`:
  - Solo permitido si `visit.status=OPEN`.
  - Actualiza campos SOAP (y opcionalmente `serviceId` **solo si** no rompe plantilla; si es complejo, NO permitir cambiar serviceId en este sprint).
- `POST /visits/{id}/close`:
  - Pasa a `CLOSED`.
- `POST /visits/{id}/reopen`:
  - Requiere `VISIT_REOPEN` + `reason` (min 10).
  - Auditoría before/after (estado + campos relevantes) siguiendo ADR-0005.

### 5.3 Adjuntos (filesystem local)

- Usar `STORAGE_DIR` (runbook) como raíz.
- Guardar archivo en subcarpeta predecible (ej: `visits/<visitId>/<attachmentId>.<ext>`).
- Validaciones:
  - tamaño <= 10MB
  - content-type permitido (pdf/jpg/png)
  - max 5 adjuntos por visita
- Configurable:
  - Implementar límites como propiedades configurables con defaults:
    - `maxSizeBytes=10MB`, `maxPerVisit=5`
  - **Si el repo ya define convención de propiedades/prefijo, usar la existente; NO inventar un estándar distinto.**
- `GET /attachments/{id}/download`:
  - Valida scope branch del recurso antes de servir.
- `DELETE /attachments/{id}`:
  - Solo si visita OPEN; borra metadata y archivo físico.

### 5.4 Plantillas SOAP

- Al crear visita:
  - Si `templateId` viene, precargar campos SOAP desde plantilla.
  - Si no viene, y existe plantilla activa para `serviceId`, se puede usar la “primera” por default **solo si** está documentado; si hay duda, NO aplicar default y exigir que el front seleccione.
- Permisos:
  - Lectura de templates: `VISIT_READ`
  - Crear/editar templates: `BRANCH_MANAGE` (ya existe; NO inventar permiso nuevo)

### 5.5 Permisos (según `docs/10-permisos.md`)

- Visitas:
  - leer: `VISIT_READ`
  - crear: `VISIT_CREATE`
  - editar SOAP: `VISIT_UPDATE`
  - cerrar: `VISIT_CLOSE`
  - reabrir: `VISIT_REOPEN` (SENSITIVE + reason + auditoría)
- Adjuntos:
  - upload: `VISIT_ATTACHMENT_UPLOAD`
  - listar/descargar: `VISIT_READ`
  - delete: `VISIT_UPDATE` (y visita OPEN)

### 5.6 Errores (Problem Details)

- Recurso no existe en branch: 404 (`VISIT_NOT_FOUND`, `TEMPLATE_NOT_FOUND`, etc.)
- Validación: 422 con `errors[]` por campo
- Visita cerrada y se intenta editar/subir adjunto/prescripción: 409 o 422 con `VISIT_CLOSED` (elegir uno consistente y documentar)
- Límite adjuntos excedido: 422 `VISIT_ATTACHMENTS_LIMIT`
- Tamaño excedido: 413 o 422 (elegir consistente; si ya hay estándar, usarlo)

## 6) Criterios de aceptación (AC)

- [ ] Backend compila y tests pasan: `./mvnw test`.
- [ ] Crear visita walk-in:
  - [ ] `POST /visits` con `petId`+`serviceId` (sin `appointmentId`) crea `status=OPEN`.
- [ ] Vincular a cita:
  - [ ] `POST /visits` con `appointmentId` válido crea y guarda vínculo.
- [ ] SOAP:
  - [ ] `PATCH /visits/{id}` actualiza campos SOAP mientras OPEN.
- [ ] Plantillas:
  - [ ] Crear template para `serviceId` (con `BRANCH_MANAGE`) y listar por service.
  - [ ] Crear visita usando `templateId` precarga texto SOAP.
- [ ] Prescripciones:
  - [ ] Crear prescripción en visita OPEN.
  - [ ] Listar prescripciones de la visita.
  - [ ] Editar prescripción mientras visita OPEN.
- [ ] Adjuntos:
  - [ ] Subir PNG/PDF < 10MB (ok) y aparece en listado metadata.
  - [ ] Descargar adjunto funciona (valida scope).
  - [ ] Subir 6to adjunto falla con error de límite.
- [ ] Cierre / reapertura:
  - [ ] `close` pasa a CLOSED.
  - [ ] Intentar `PATCH` o subir adjunto/prescripción en CLOSED falla con `VISIT_CLOSED`.
  - [ ] `reopen` sin reason falla (422).
  - [ ] `reopen` con reason (>=10) funciona y genera auditoría before/after.
- [ ] Smoke:
  - [ ] `scripts/smoke/spr-b005.ps1` existe y cubre: login vet → crear visita → patch SOAP → prescripción → upload adjunto → close → reopen con reason.
- [ ] Evidencia docs:
  - [ ] `docs/log/log.md` entrada append-only `SPR-B005`
  - [ ] `docs/status/status.md` fila `SPR-B005` en `READY_FOR_VALIDATION` con hash commit
  - [ ] `docs/traceability/rtm.md` actualizado:
    - BRD-REQ-024..029 → `READY_FOR_VALIDATION`
    - BRD-REQ-030 → permanece `PLANNED/PENDING` con nota “export en SPR-B006”
  - [ ] `docs/state/state.md` actualizado con next sprint recomendado: `SPR-B006`

## 7) Smoke test manual (usuario)

1) Levantar backend:
- `cd backend`
- `./mvnw spring-boot:run`

2) Ejecutar smoke:
- `pwsh -File scripts/smoke/spr-b005.ps1`

3) Evidencia:
- Pegar output en `docs/log/log.md` en la entrada de `SPR-B005` (o dejar placeholder explícito).

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
- `docs/traceability/rtm.md` actualizado con evidencia/verificación.
- `docs/state/state.md` actualizado (snapshot + next sprint).

## 10) Si hay huecos/contradicciones

- Prohibido editar este sprint.
- Si hay contradicción de IDs (ej: `docs/06-dominio-parte-b.md` referencia IDs distintos):
  - Corregir **solo la referencia** para alinear con `docs/02-brd.md` (esto NO cambia BRD).
- Si aparece una decisión arquitectónica faltante (ej: convención de ruta en STORAGE_DIR impacta estructura global):
  - Crear ADR nuevo (siguiente número disponible) y registrar en `docs/changelog.md`.
  - Si bloquea: marcar `SPR-B005` como `BLOCKED`, registrar en LOG y DETENER.

<!-- EOF -->

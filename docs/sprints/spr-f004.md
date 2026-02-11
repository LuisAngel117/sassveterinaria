# SPR-F004 — Atención / SOAP UI (plantillas + adjuntos + cierre/reapertura)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)
**Stage:** 4
**Duración objetivo:** 45–90 min (referencial)
**Tipo:** Frontend

## 1) Objetivo

- Entregar el módulo clínico usable para demo local (rol Veterinario/Admin):
  - Crear atención **walk-in** (sin cita) y/o abrir atención desde agenda (si ya existe enlace).
  - Editar SOAP mínimo (S/O/A/P) por atención.
  - Registrar prescripciones estructuradas por atención.
  - Subir adjuntos (PDF/JPG/PNG) por atención.
  - Cerrar atención (bloquear edición) y reabrir con permiso + reason.
- Integración real contra backend B005 (sin inventar contratos/campos).
- Declarar trazabilidad objetivo (según master FRONT):
  - BRD-REQ-024..029

## 2) Alcance

### Incluye

- Navegación “Atenciones” (visible por rol/permisos).
- Pantallas/rutas (en el router existente; sin re-arquitectura):
  - Cola/lista de atenciones (al menos “hoy” o “abiertas”, según endpoint real).
  - Crear atención (walk-in) usando `POST /api/v1/visits`.
  - Detalle/edición atención:
    - SOAP mínimo editable (PATCH).
    - Prescripciones: agregar y listar (según endpoint real).
    - Adjuntos: listar + subir (multipart) + descargar/ver (si endpoint existe).
    - Acciones: Cerrar / Reabrir (con reason cuando aplique).
- Permisos UI (ocultar/deshabilitar acciones):
  - `VISIT_READ`, `VISIT_CREATE`, `VISIT_UPDATE`, `VISIT_CLOSE`, `VISIT_REOPEN`, `VISIT_ATTACHMENT_UPLOAD` (según `docs/10-permisos.md`).
  - Si backend usa strings diferentes en `/api/v1/me`, respetar lo real (no inventar).
- Manejo de errores consistente (Problem Details RFC7807):
  - Mapear `fieldErrors[]` a inputs.
  - 401/403 mensajes humanos en español.
  - Conflictos/validaciones (400/409/422) con mensajes accionables.

### Excluye

- No construir facturación UI (SPR-F005).
- No construir inventario UI (SPR-F006).
- No construir auditoría UI (SPR-F008).
- No construir “exportar indicaciones” en UI si no está explícito en BRD para este sprint (BRD-REQ-030 queda fuera del cierre de este sprint).
- No tocar backend. Si falta endpoint/contrato, RFC y detener.

## 3) Pre-check (obligatorio para Codex)

- `git status` limpio (si no, detener).
- `git config user.name` y `git config user.email` presentes (si no, detener).
- Rama actual (`git rev-parse --abbrev-ref HEAD`).
- Existe este archivo: `docs/sprints/spr-f004.md`.
- Lectura obligatoria antes de tocar código:
  - `docs/state/state.md`
  - `docs/quality/definition-of-ready.md`
  - `docs/quality/definition-of-done.md`
  - `docs/02-brd.md` (BRD-REQ-024..029)
  - `docs/03-arquitectura.md` (scoping `X-Branch-Id`, Problem Details)
  - `docs/05-seguridad.md` (401/403, auth, refresh)
  - `docs/07-ux-ui-parte-a.md` y `docs/07-ux-ui-parte-b.md` (flujo clínico + UX)
  - `docs/10-permisos.md` (VISIT_*)
  - `docs/08-runbook.md` (endpoints reales de visitas + límites adjuntos + credenciales demo)
  - `docs/sprints/spr-master-front.md`
- Dependencia BACK:
  - Confirmar en `docs/state/state.md` y/o `docs/status/status.md` que `SPR-B005` está al menos `READY_FOR_VALIDATION`.
  - Si NO lo está: marcar este sprint como `BLOCKED` (nota: dependencia B005) y DETENER.
- Fuente de verdad de contratos:
  - Prioridad 1: OpenAPI del backend (`/v3/api-docs`) / Swagger UI.
  - Prioridad 2: `docs/08-runbook.md` (smoke B005 lista endpoints mínimos).
  - Si hay contradicción entre docs: usar backend (OpenAPI) como fuente; crear RFC si el conflicto afecta decisiones.

## 4) Entregables

- Frontend:
  - Módulo “Atenciones” funcional end-to-end contra backend:
    - listar/abrir atención (según endpoint real)
    - crear atención walk-in (`POST /api/v1/visits`)
    - editar SOAP (`PATCH /api/v1/visits/{id}` o endpoint real)
    - prescripciones (`POST /api/v1/visits/{id}/prescriptions` + listar si existe)
    - adjuntos (`POST /api/v1/visits/{id}/attachments` multipart + listar si existe)
    - cerrar (`POST /api/v1/visits/{id}/close`)
    - reabrir (`POST /api/v1/visits/{id}/reopen` con `reason` >= 10)
  - UX vendible (sin pantallas vacías si hay seed demo).
  - Manejo de permisos y errores consistente.
- Docs (al ejecutar el sprint):
  - `docs/traceability/rtm.md` actualizado para BRD-REQ-024..029 → SPR-F004 (con evidencia commit).
  - `docs/state/state.md` snapshot actualizado (próximo sprint recomendado).
  - `docs/log/log.md` append-only con comandos + outputs/placeholders.
  - `docs/status/status.md` actualizado a `READY_FOR_VALIDATION` (nunca DONE).

## 5) Instrucciones de implementación (cerradas)

> REGLA: NO inventar shapes/campos. Todo request/response sale de OpenAPI.
> Los endpoints mínimos (si OpenAPI confirma lo mismo) están listados en `docs/08-runbook.md` (smoke B005).

Pasos:

1) **Identificar FRONT_DIR y router real**
- Ubicar el `package.json` del frontend (Next.js).
- Detectar router:
  - App Router (`app/` o `src/app/`) vs Pages Router (`pages/` o `src/pages/`).
- Implementar rutas dentro del router ya usado.
- Si hay ambigüedad (más de un frontend o router no determinable) → RFC y DETENER.

2) **Descubrir contratos reales de atenciones (visits)**
- Confirmar (por OpenAPI) existencia y shape de:
  - `POST /api/v1/visits` (walk-in) — requerido por runbook.
  - `PATCH /api/v1/visits/{id}` (SOAP update) — requerido por runbook.
  - `POST /api/v1/visits/{id}/prescriptions` — requerido por runbook.
  - `POST /api/v1/visits/{id}/attachments` (multipart) — requerido por runbook.
  - `POST /api/v1/visits/{id}/close` — requerido por runbook.
  - `POST /api/v1/visits/{id}/reopen` — requerido por runbook (reason >= 10).
- Para listado/cola:
  - Buscar endpoint real (ej. `GET /api/v1/visits`, `GET /api/v1/visits?status=...`, `GET /api/v1/pets/{id}/visits`, etc.).
  - Si NO existe endpoint de listado utilizable:
    - (Opción A) implementar pantalla “Atención” accesible solo desde:
      - agenda (abrir visita desde cita) y/o
      - “Última visita demo” (si seed expone ID por algún endpoint).
    - (Opción B) crear RFC para pedir endpoint de listado (si bloquea demo).
    - No inventar endpoint.

3) **Permisos reales**
- Confirmar en OpenAPI y/o `/api/v1/me` (o endpoint equivalente) los códigos reales que llegan al frontend.
- UI gate:
  - Visualización módulo: requiere `VISIT_READ` o equivalente real.
  - Crear: `VISIT_CREATE`
  - Editar SOAP/prescripciones: `VISIT_UPDATE`
  - Subir adjunto: `VISIT_ATTACHMENT_UPLOAD`
  - Cerrar: `VISIT_CLOSE`
  - Reabrir: `VISIT_REOPEN` + reason required
- Si se detecta contradicción entre `docs/07-ux-ui-parte-b.md` y `docs/10-permisos.md`:
  - usar backend + `docs/10-permisos.md` como guía;
  - crear RFC de alineación documental (no editar docs fuera del scope del sprint).

4) **API layer (reusar; no duplicar)**
- Reusar el cliente HTTP existente (SPR-F001).
- Agregar funciones mínimas (nombres internos libres; contrato NO):
  - `createVisit(payload)`
  - `updateVisitSoap(visitId, payload)` (o endpoint real)
  - `addPrescription(visitId, payload)`
  - `listPrescriptions(visitId)` si existe; si no existe endpoint, mantener solo alta y reflejar resultado con re-fetch del detalle si existe.
  - `uploadVisitAttachment(visitId, file, meta)` con `FormData`
  - `listAttachments(visitId)` si existe
  - `closeVisit(visitId)` (POST)
  - `reopenVisit(visitId, reason)` (POST)
- Headers:
  - `Authorization: Bearer <access>`
  - `X-Branch-Id: <branchId actual>` (branch-scoped)
- Errores:
  - mapear Problem Details:
    - `title`/`detail` a banner/alert
    - `fieldErrors[]` a inputs
  - 401: “Sesión expirada, vuelve a iniciar sesión”
  - 403: “Sin permiso para esta acción”

5) **UI: Atenciones (cola/lista)**
- Diseñar lista vendible, mínima:
  - Filtros: estado (abierta/cerrada) si el backend lo soporta; si no, omitir.
  - Campos visibles (si están en response real): mascota, cliente, servicio, estado, fecha/hora, sala, veterinario.
  - Acción “Abrir” (detalle).
- CTA “Nueva atención (walk-in)” si permiso `VISIT_CREATE`.

6) **UI: Crear atención (walk-in)**
- Form mínimo (según OpenAPI):
  - Debe permitir seleccionar:
    - cliente + mascota (o mascota directamente) usando endpoints de CRM ya integrados (SPR-F003) si existen.
    - servicio (catálogo) si backend lo pide.
  - Si backend permite `appointmentId` opcional: dejar campo oculto o usarlo solo cuando venga desde agenda (no inventar).
- Al crear:
  - navegar a detalle de la atención creada.

7) **UI: Detalle/edición atención (SOAP)**
- Secciones (tabs o bloques):
  - “SOAP” (S/O/A/P) con campos mínimos BRD-REQ-025:
    - S: motivo_consulta, anamnesis
    - O: peso, temperatura, hallazgos
    - A: diagnostico, severidad (si existe)
    - P: plan_tratamiento, indicaciones, recontrol (fecha opcional)
  - “Prescripciones” (BRD-REQ-029):
    - medicamento, dosis, unidad, frecuencia, duración, vía, observaciones
  - “Adjuntos” (BRD-REQ-027):
    - lista (si existe) con nombre, tipo, tamaño, fecha
    - botón subir (si permiso)
- Reglas de estado:
  - Si la atención está “cerrada”:
    - bloquear inputs y botones de edición
    - mostrar badge “CERRADA”
    - mostrar acción “Reabrir” solo si permiso
  - Si está “abierta”:
    - permitir editar y “Cerrar atención” si permiso
- Reason modal:
  - Para reabrir: pedir “Motivo (reason)” obligatorio (min 10 chars) y mostrar nota “Esta acción quedará auditada”.
  - Para cerrar: reason NO requerido por BRD (no pedir, salvo que backend lo exija).

8) **Adjuntos (multipart)**
- UI debe mostrar límites en texto:
  - “Máx 10MB por archivo, máx 5 adjuntos por atención (configurable)”.
- Implementación:
  - usar `FormData` con el nombre de campo EXACTO según OpenAPI.
  - validar en UI tamaño <= 10MB (por defecto) antes de enviar, pero si OpenAPI define otro límite, usar el real.
  - manejar error cuando se excede el máximo por visita (mostrar mensaje humano).

9) **Cierre del sprint**
- Ejecutar comandos “verdad” (sección 8).
- Actualizar:
  - `docs/log/log.md` (append-only) con outputs o placeholders
  - `docs/status/status.md` → `READY_FOR_VALIDATION` con hash commit
  - `docs/traceability/rtm.md` para BRD-REQ-024..029 → evidencia del commit
  - `docs/state/state.md` snapshot y siguiente sprint recomendado
- Commit: `SPR-F004: atenciones SOAP + prescripciones + adjuntos + close/reopen`

## 6) Criterios de aceptación (AC)

- [ ] Existe módulo “Atenciones” accesible desde navegación según permisos.
- [ ] Se puede crear atención walk-in usando `POST /api/v1/visits` (payload real).
- [ ] Se puede editar SOAP mínimo con `PATCH /api/v1/visits/{id}` (payload real).
- [ ] Se puede agregar prescripción estructurada con `POST /api/v1/visits/{id}/prescriptions`.
- [ ] Se puede subir adjunto con `POST /api/v1/visits/{id}/attachments` (multipart) respetando límites.
- [ ] Se puede cerrar atención con `POST /api/v1/visits/{id}/close` y la UI bloquea edición al estar cerrada.
- [ ] Se puede reabrir atención con `POST /api/v1/visits/{id}/reopen` solicitando reason (min 10) y respetando permiso.
- [ ] Manejo de errores Problem Details:
  - [ ] `fieldErrors[]` mapea a inputs
  - [ ] 401/403 mensajes humanos en español
- [ ] `npm run build` (o script real equivalente) pasa.
- [ ] `docs/status/status.md` actualizado a `READY_FOR_VALIDATION` con evidencia (commit hash).
- [ ] `docs/log/log.md` append-only con sección SPR-F004 y placeholders/outputs.
- [ ] `docs/traceability/rtm.md` actualizado para BRD-REQ-024..029 apuntando a SPR-F004.
- [ ] `docs/state/state.md` actualizado (snapshot).

## 7) Smoke test manual (usuario)

Precondición: backend + DB local corriendo según `docs/08-runbook.md`.

1) Levantar backend (si no está):
- `cd backend`
- `./mvnw spring-boot:run`

2) Levantar frontend:
- `cd <FRONT_DIR>`
- `npm install` (o el comando real según lockfile)
- `npm run dev`

3) Flujo UI (usuario `veterinario`):
- Login: `veterinario / Veterinario123!`
- Seleccionar sucursal (ej. `CENTRO`)
- Ir a “Atenciones”
- Crear una atención walk-in (usando cliente/mascota demo si existen)
- Completar SOAP mínimo y guardar
- Agregar 1 prescripción
- Subir 1 adjunto PNG/JPG/PDF (< 10MB)
- Cerrar atención
- Intentar editar SOAP: debe estar bloqueado
- Reabrir con reason (>= 10 chars) si el rol tiene permiso
- Verificar que vuelve a permitir edición

**Evidencia:** PEGAR OUTPUT EN `docs/log/log.md` en la entrada de SPR-F004.

## 8) Comandos verdad

- Frontend (en `<FRONT_DIR>`):
  - `npm run build`
  - `npm run dev`

Si el proyecto NO tiene esos scripts: **N/A** con razón en LOG + RFC (no inventar).

## 9) DoD

- AC completos.
- `docs/status/status.md` en `READY_FOR_VALIDATION` (nunca DONE).
- `docs/log/log.md` actualizado (append-only).
- `docs/traceability/rtm.md` y `docs/state/state.md` actualizados.
- Cumplir `docs/quality/definition-of-done.md`.

## 10) Si hay huecos/contradicciones

- Si falta endpoint de listado de atenciones y bloquea la UI vendible:
  - crear RFC en `docs/rfcs/` describiendo el hueco (con evidencia de OpenAPI) y DETENER.
- Si hay contradicción de permisos (ej. UX vs Permisos):
  - seguir backend + `docs/10-permisos.md`;
  - crear RFC de alineación documental (sin editar docs fuera del scope).
- NO inventar contratos/campos.
- Dejar repo compilable.

<!-- EOF -->

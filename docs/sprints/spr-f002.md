# SPR-F002 — Agenda UI (semana) + crear/editar + estados + conflictos + check-in + bloqueos

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)
**Stage:** 2
**Duración objetivo:** 45–90 min (referencial)
**Tipo:** Frontend

## 1) Objetivo

- Entregar **Agenda semanal** integrada contra backend real: listar citas por rango semanal, filtrar por sala y estado, y operar el flujo mínimo de recepción.
- Implementar **CRUD operativo de cita** (crear/editar) y **transiciones** mínimas de estado, incluyendo:
  - conflicto por no-solape (mostrar error y alternativa de sobre-cupo solo si permiso),
  - check-in separado de “en atención”.
- Implementar **bloqueos manuales** (si el backend expone contrato) o bloquear por RFC (si no existe contrato: no inventar).
- Declarar trazabilidad objetivo (este sprint pretende cerrar):
  - BRD-REQ-010, BRD-REQ-011, BRD-REQ-012, BRD-REQ-013, BRD-REQ-014, BRD-REQ-015

## 2) Alcance

### Incluye

- Ruta/pantalla **Agenda** con vista semanal (sin asumir grid de “slots”: se representa por día + lista ordenada por hora; si el backend provee slot/granularidad/config, se usa esa).
- Filtros:
  - por **sala** (rooms),
  - por **estado** (reservado/confirmado/en_atencion/cerrado/cancelado + cualquier otro estado real expuesto).
- Crear cita:
  - seleccionar sala + servicio + cliente + mascota + fecha/hora,
  - validar y mostrar errores del backend (Problem Details).
- Editar cita:
  - actualizar campos permitidos por el backend (sin inventar),
  - transicionar estado según contrato real.
- Conflictos (no-solape):
  - si backend responde conflicto (típicamente 409), mostrar detalle (mensaje humano en español),
  - permitir reintento con “sobre-cupo” SOLO si permiso `APPT_OVERBOOK` y SOLO si el backend soporta flag/campo para ello.
  - acciones sensibles: capturar `reason` cuando el backend lo requiera (no inventar nombre del campo; se extrae de OpenAPI).
- Check-in:
  - botón “Check-in” separado de “Iniciar atención” (BRD-REQ-014),
  - usar el endpoint real (si existe) o el campo permitido (si el backend lo define así).
- Bloqueos manuales (BRD-REQ-015):
  - si OpenAPI expone recurso/endpoint para “bloqueo” o “slot bloqueado”: implementar UI mínima para crear y listar.
  - si NO existe contrato backend para bloqueos: crear RFC y DETENER (no inventar).

### Excluye

- No construir cola “hoy” avanzada para veterinario (eso puede ir en F004/F009 si se decide).
- No construir pantallas completas de clientes/mascotas (F003), pero sí se permite “selector”/búsqueda mínima para crear cita usando endpoints reales.
- No implementar drag&drop ni resizing de citas (solo formularios).
- No implementar reporting (F007) ni auditoría UI (F008).
- No alterar contratos backend ni “arreglar” endpoints en backend desde este sprint.

## 3) Pre-check (obligatorio para Codex)

- `git status` limpio (si no, detener).
- `git config user.name` y `git config user.email` presentes (si no, detener).
- Rama actual (`git rev-parse --abbrev-ref HEAD`).
- Existe este archivo: `docs/sprints/spr-f002.md`.
- Lectura obligatoria antes de tocar código:
  - `docs/state/state.md`
  - `docs/quality/definition-of-ready.md`
  - `docs/quality/definition-of-done.md`
  - (si existe) `AGENTS.md`
  - `docs/03-arquitectura.md` (scoping `X-Branch-Id`, errores)
  - `docs/05-seguridad.md` (acciones sensibles + reason, roles/permisos)
  - `docs/10-permisos.md` (permisos Agenda: `APPT_*`)
  - `docs/07-ux-ui-parte-a.md` (flujos críticos)
  - `docs/08-runbook.md` (cómo levantar y seeds demo)
  - `docs/sprints/spr-master-front.md`
- Dependencia BACK:
  - Revisar `docs/status/status.md` y confirmar que `SPR-B002` está al menos `READY_FOR_VALIDATION`.
  - Si no lo está: marcar este sprint como `BLOCKED` (nota: dependencia) y DETENER.
- Contrato BACK→FRONT:
  - Si existe `docs/handoff/handoff-back-to-front.md`, leerlo.
  - Si NO existe, NO inventar: el contrato se debe derivar de OpenAPI (`/v3/api-docs`) y/o scripts smoke del repo.

## 4) Entregables

- Frontend:
  - Pantalla/ruta “Agenda semanal”.
  - Componentes:
    - Filtros (sala/estado/semana),
    - Lista semanal por día con cards de cita,
    - Modal formulario Crear/Editar,
    - Modal “Motivo” reutilizable para acciones sensibles (cancelar/sobre-cupo y cualquier otra que el backend marque sensible).
  - Integración real por HTTP:
    - listados de salas (si aplica),
    - listados de citas por rango,
    - create/update,
    - check-in,
    - (si aplica) endpoints de bloqueos.
  - Manejo de permisos desde la sesión (derivado del contrato real de `/me`):
    - ocultar/deshabilitar acciones sin permiso (por lo menos: create/update/cancel/overbook/checkin/start).
- Docs (al ejecutar el sprint):
  - `docs/traceability/rtm.md` actualizado para BRD-REQ-010..015 → SPR-F002 (con evidencia commit).
  - `docs/state/state.md` snapshot actualizado (próximo sprint recomendado).
  - `docs/log/log.md` append-only con comandos + outputs/placeholders.
  - `docs/status/status.md` actualizado a `READY_FOR_VALIDATION` (nunca DONE).

## 5) Instrucciones de implementación (cerradas)

> REGLA: NO asumir rutas ni shapes. Todo debe salir de:
> - OpenAPI del backend (`/v3/api-docs` o Swagger UI en el repo), y/o
> - scripts smoke existentes en `scripts/smoke/*.ps1`, y/o
> - `docs/state/state.md` (endpoints mencionados), y/o
> - `docs/handoff/handoff-back-to-front.md` si existe.

Pasos:

1) **Identificar el FRONT_DIR** (root real del frontend)
   - Localizar el `package.json` del frontend (Next.js).
   - Si hay más de un candidato y no hay referencia clara en docs: crear RFC `docs/rfcs/rfc-front-root.md` y DETENER.

2) **Confirmar router en uso**
   - App Router (`app/` o `src/app/`) vs Pages Router (`pages/` o `src/pages/`).
   - Implementar dentro del router real ya existente.

3) **Descubrir contratos reales (Agenda)**
   - Desde OpenAPI y/o smoke scripts, extraer:
     - Endpoint para listar citas por rango (parámetros `from/to` o equivalente).
     - Endpoint para crear cita y campos obligatorios (room/service/client/pet + timestamps).
     - Endpoint para actualizar cita y campos permitidos.
     - Cómo se expresa el estado (enum real) y cómo se transiciona (endpoint dedicado o patch).
     - Contrato de conflicto no-solape: status code y payload (Problem Details recomendado).
     - Mecanismo de sobre-cupo: flag/campo real + si requiere `reason`.
     - Endpoint/campo de check-in.
     - Endpoints/contrato de bloqueos manuales (BRD-REQ-015) si existen.
   - Si falta cualquiera de estos contratos en OpenAPI/smoke: crear RFC y DETENER (no inventar).

4) **Modelo de permisos en UI**
   - Determinar desde el contrato de `/me`:
     - dónde vienen permisos (por ejemplo `permissions: string[]`),
     - o si vienen por rol y se mapean.
   - Guardar permisos en la sesión (si no existe aún).
   - UI debe respetar al menos:
     - `APPT_READ`, `APPT_CREATE`, `APPT_UPDATE`, `APPT_CANCEL`, `APPT_OVERBOOK`, `APPT_CHECKIN`, `APPT_START_VISIT`.

5) **Implementar API layer (sin duplicar)**
   - Reusar el cliente API existente de SPR-F001.
   - Agregar funciones mínimas:
     - `listRooms()` (si aplica),
     - `listAppointments({from,to,roomId,status})`,
     - `createAppointment(payload)`,
     - `updateAppointment(id,payload)` o transición según contrato,
     - `checkInAppointment(id, payload?)`,
     - `createBlock(...)` / `listBlocks(...)` si existe contrato.
   - Mantener `X-Branch-Id` según selección actual.

6) **Pantalla Agenda (vista semanal sin asumir slots)**
   - Selector de semana:
     - “Semana actual” + botones anterior/siguiente.
     - Rango `from/to` calculado (lunes→domingo) en timezone negocio (solo para display; payload siempre en ISO-8601 como backend defina).
   - Filtros:
     - sala (si aplica) y estado.
   - Render:
     - 7 columnas (días) con lista ordenada por hora.
     - Cada cita muestra: hora inicio/fin, cliente+mascota (si el backend lo incluye; si no, mostrar IDs y un TODO para enriquecer cuando existan endpoints de detalle), estado con badge, sala/servicio si viene.
   - Estados vacíos: mensaje claro + CTA “Crear cita” si hay permiso.

7) **Modal Crear/Editar cita**
   - Campos mínimos (según contrato real):
     - sala, servicio, cliente, mascota, inicio, (duración/fin si aplica), notas.
   - Búsqueda cliente/mascota:
     - usar endpoints reales de CRM (B003) para buscar/seleccionar, sin construir pantallas completas.
   - Submit:
     - manejar 400/401/403 con mensajes humanos.
     - manejar conflicto no-solape:
       - mostrar detalle,
       - si el usuario tiene `APPT_OVERBOOK` y el backend soporta “sobreCupo”: permitir reintento con modal de motivo (reason) y flag correspondiente.

8) **Acciones sobre cita (según permisos y contrato)**
   - Check-in:
     - botón visible solo con `APPT_CHECKIN`.
     - acción llama endpoint real y actualiza UI.
   - Iniciar atención:
     - solo si backend soporta transición desde cita (puede crear/abrir visita).
     - visible con `APPT_START_VISIT` (y/o permiso real si difiere).
     - si no existe contrato en OpenAPI: RFC y ocultar (no inventar).
   - Cancelar:
     - visible con `APPT_CANCEL`.
     - si backend exige `reason`: mostrar modal de motivo y enviar campo real.
     - actualizar UI.

9) **Bloqueos manuales (BRD-REQ-015)**
   - Si el backend expone contrato de bloqueos:
     - permitir crear bloqueo simple (rango + sala + motivo),
     - listar y mostrar en agenda (como item bloqueado).
   - Si NO existe contrato: RFC `docs/rfcs/rfc-blocks-contract.md` y DETENER (porque no se puede cerrar BRD-REQ-015 sin inventar).

10) **Cierre del sprint**
   - Ejecutar comandos “verdad” (ver sección 8).
   - Actualizar:
     - `docs/log/log.md` (append-only) con outputs o placeholders
     - `docs/status/status.md` → `READY_FOR_VALIDATION` con hash commit
     - `docs/traceability/rtm.md` para BRD-REQ-010..015 → evidencia del commit
     - `docs/state/state.md` snapshot y siguiente sprint recomendado
   - Commit: `SPR-F002: agenda semana + crear/editar + conflictos + check-in (+ bloqueos si contrato existe)`

## 6) Criterios de aceptación (AC)

- [ ] Existe pantalla de Agenda semanal y lista citas del backend por rango semanal (sin inventar endpoints).
- [ ] Filtros por sala/estado funcionan (si el backend soporta; si no, RFC y AC ajustado por bloqueo, no por invento).
- [ ] Crear cita funciona y se refleja en la lista semanal.
- [ ] Editar cita funciona (para campos permitidos por backend).
- [ ] Conflicto no-solape se muestra con mensaje humano; si usuario tiene `APPT_OVERBOOK` y backend soporta, existe opción de reintento con sobre-cupo + motivo cuando corresponda.
- [ ] Check-in está separado de “en atención” y ejecuta contrato real.
- [ ] Acciones sensibles usan modal de motivo cuando el backend lo exige.
- [ ] Bloqueos manuales:
  - [ ] si existe contrato backend: se pueden crear y se muestran,
  - [ ] si NO existe contrato: RFC creado y sprint queda BLOCKED (no inventar).
- [ ] `npm run build` (o script real equivalente del FRONT_DIR) pasa.
- [ ] `docs/status/status.md` actualizado a `READY_FOR_VALIDATION` con evidencia (commit hash).
- [ ] `docs/log/log.md` append-only con sección SPR-F002 y placeholders/outputs.
- [ ] `docs/traceability/rtm.md` actualizado para BRD-REQ-010..015 apuntando a SPR-F002.
- [ ] `docs/state/state.md` actualizado (snapshot).

## 7) Smoke test manual (usuario)

1) Levantar backend + Postgres local según `docs/08-runbook.md` y asegurar seed demo cargado (B011).
2) Levantar frontend:
   - `cd <FRONT_DIR>`
   - `npm install` (o el comando real según lockfile)
   - `npm run dev`
3) En navegador:
   - Login con credenciales demo
   - Ir a “Agenda”
   - Crear una cita en una sala y verificar que aparece
   - Intentar crear otra que solape y verificar conflicto
   - Si el usuario demo tiene `APPT_OVERBOOK`: probar sobre-cupo con motivo
   - Probar check-in

**Evidencia:** PEGAR OUTPUT EN `docs/log/log.md` en la entrada de SPR-F002.

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

- Crear RFC/ADR/CHANGELOG según corresponda.
- Detener si bloquea (no inventar contratos ni campos).
- Dejar repo compilable.

<!-- EOF -->

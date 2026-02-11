# SPR-F003 — Clientes y Mascotas UI (CRUD + búsqueda)

**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)
**Stage:** 3
**Duración objetivo:** 45–90 min (referencial)
**Tipo:** Frontend

## 1) Objetivo

- Entregar módulo **CRM** usable y vendible en local:
  - **Clientes (propietarios)**: lista + búsqueda + crear + editar + ficha.
  - **Mascotas**: listar por cliente + crear + editar + ficha básica.
- Integración real contra backend (CRM B003) sin inventar contratos.
- Respetar scoping por sucursal (branch) y permisos (hide/disable).
- Declarar trazabilidad objetivo:
  - BRD-REQ-016 (CRUD clientes)
  - BRD-REQ-017 (búsqueda clientes)
  - BRD-REQ-018 (CRUD mascotas)
  - BRD-REQ-019 (código interno mascota único por sucursal)
  - BRD-REQ-020 (una mascota tiene un solo propietario v1)

## 2) Alcance

### Incluye

- Módulo “Clientes” en navegación (visible según permisos).
- Pantallas/rutas (en el router real existente; sin re-arquitectura):
  - Lista de clientes con búsqueda (nombre/teléfono/identificación).
  - Crear cliente.
  - Editar cliente.
  - Ficha cliente con pestaña/tabla de mascotas y CTA “Agregar mascota”.
  - Crear mascota (dentro del contexto de un cliente).
  - Editar mascota (sin permitir cambiar propietario en v1; BRD-REQ-020).
- Validaciones UI mínimas (sin duplicar reglas del backend):
  - identificación opcional; si se ingresa, validar formato básico (ver instrucciones).
  - email formato.
  - peso numérico (>= 0).
  - código interno mascota requerido/único por sucursal: validación primaria en backend; UI muestra error humano si hay conflicto.
- Manejo de errores consistente (Problem Details):
  - 400 con fieldErrors → mapear a inputs.
  - 401/403 → mensajes claros + respeto de permisos.
  - 409/422 (si aplica) → mostrar conflicto humano.

### Excluye

- No construir “Historia clínica” dentro de CRM (eso es SPR-F004).
- No construir “merge” de clientes, ni multi-propietario por mascota.
- No construir import/export masivo (CSV) de CRM.
- No implementar deduplicación avanzada ni validación fuerte de cédula (solo formato básico v1).
- No tocar backend (si falta contrato, RFC y detener).

## 3) Pre-check (obligatorio para Codex)

- `git status` limpio (si no, detener).
- `git config user.name` y `git config user.email` presentes (si no, detener).
- Rama actual (`git rev-parse --abbrev-ref HEAD`).
- Existe este archivo: `docs/sprints/spr-f003.md`.
- Lectura obligatoria antes de tocar código:
  - `docs/state/state.md`
  - `docs/quality/definition-of-ready.md`
  - `docs/quality/definition-of-done.md`
  - (si existe) `AGENTS.md`
  - `docs/02-brd.md` (BRD-REQ-016..020)
  - `docs/03-arquitectura.md` (scoping `X-Branch-Id`, Problem Details)
  - `docs/05-seguridad.md` (modelos de auth, 401/403)
  - `docs/07-ux-ui-parte-a.md` y `docs/07-ux-ui-parte-b.md` (pantallas/validaciones UX)
  - `docs/10-permisos.md` (CLIENT_*, PET_*)
  - `docs/08-runbook.md` (seeds demo + cómo levantar local)
  - `docs/sprints/spr-master-front.md`
- Dependencia BACK:
  - Revisar `docs/status/status.md` y confirmar que `SPR-B003` está al menos `READY_FOR_VALIDATION`.
  - Si no lo está: marcar este sprint como `BLOCKED` (nota: dependencia) y DETENER.
- Contratos:
  - Preferir `docs/handoff/handoff-back-to-front.md` si existe.
  - Si NO existe: derivar TODO contrato de CRM desde OpenAPI (`/v3/api-docs`) y/o scripts smoke del repo. Si no hay fuente de verdad → RFC y DETENER.

## 4) Entregables

- Frontend:
  - Pantalla “Clientes” (lista + búsqueda + acciones).
  - Pantalla “Cliente: ficha” (datos + mascotas).
  - Formularios Cliente/Mascota (crear/editar) con validación mínima.
  - Integración API real:
    - list/search clients
    - create/update client
    - list pets by client (o por query)
    - create/update pet
  - Respeto de permisos:
    - `CLIENT_READ/CREATE/UPDATE`
    - `PET_READ/CREATE/UPDATE`
- Docs (al ejecutar el sprint):
  - `docs/traceability/rtm.md` actualizado para BRD-REQ-016..020 → SPR-F003 (con evidencia commit).
  - `docs/state/state.md` snapshot actualizado (próximo sprint recomendado).
  - `docs/log/log.md` append-only con comandos + outputs/placeholders.
  - `docs/status/status.md` actualizado a `READY_FOR_VALIDATION` (nunca DONE).

## 5) Instrucciones de implementación (cerradas)

> REGLA: NO asumir rutas, params, ni shapes. Todo debe salir de:
> - OpenAPI del backend (`/v3/api-docs` / Swagger UI), o
> - scripts smoke existentes (si los hay para CRM), o
> - docs existentes (state/runbook/handoff si existe).

Pasos:

1) **Identificar FRONT_DIR y router real**
   - Ubicar el `package.json` del frontend (Next.js).
   - Detectar router:
     - App Router (`app/` o `src/app/`) vs Pages Router (`pages/` o `src/pages/`).
   - Implementar rutas dentro del router ya usado.
   - Si hay ambigüedad (más de un frontend o router no determinable) → RFC y DETENER.

2) **Descubrir contratos reales CRM (clientes/mascotas)**
   - Desde OpenAPI y/o smoke scripts, extraer:
     - endpoint para listar/buscar clientes (query param real: por ejemplo `q`, `search`, filtros; NO inventar),
     - endpoint para crear cliente (campos requeridos),
     - endpoint para actualizar cliente (PATCH/PUT y campos permitidos),
     - endpoint para obtener detalle de cliente (si existe),
     - endpoints para mascotas:
       - listar mascotas por cliente (si existe) o por query,
       - crear mascota (campos requeridos),
       - actualizar mascota,
       - detalle mascota (si existe),
     - cómo se expresa branch scoping (debe usar `X-Branch-Id` vía cliente API ya existente).
   - Si no hay endpoints claros para mascotas o clientes → RFC `docs/rfcs/rfc-crm-contract-missing.md` y DETENER.

3) **Permisos y guardas**
   - Reusar el mecanismo de sesión/permisos existente (SPR-F001).
   - En navegación:
     - “Clientes” visible solo si `CLIENT_READ` o rol equivalente permitido.
   - En acciones:
     - botones Crear/Editar solo si `CLIENT_CREATE/CLIENT_UPDATE`.
     - mascotas: `PET_*`.

4) **API layer (reusar; no duplicar)**
   - Reusar el cliente API central (SPR-F001).
   - Agregar funciones mínimas (nombres internos libres, contrato no):
     - `searchClients(...)` o `listClients(...)` según contrato real.
     - `getClient(id)` si existe.
     - `createClient(payload)`
     - `updateClient(id,payload)`
     - `listPetsByClient(clientId)` o equivalente real.
     - `getPet(id)` si existe.
     - `createPet(payload)` (asociada a clientId según contrato real).
     - `updatePet(id,payload)`
   - Errores:
     - si backend devuelve Problem Details: mapear `title/detail` y `fieldErrors[]` a inputs.

5) **Diseño UI (vendible, simple, sin “pantallas vacías”)**
   - Clientes (lista):
     - input de búsqueda (placeholder: “Buscar por nombre, teléfono o identificación”).
     - tabla/lista con: nombre, teléfono, identificación (si existe), #mascotas (si backend lo provee; si no, omitir).
     - CTA “Nuevo cliente” (si permiso).
   - Cliente (ficha):
     - sección “Datos del cliente” (ver/editar).
     - sección “Mascotas”:
       - lista con nombre, especie, código interno, alertas (si existe).
       - CTA “Agregar mascota” (si permiso).
   - Mascota (form):
     - campos mínimos según BRD:
       - nombre (si existe en backend; si no, usar el campo real),
       - especie, raza, sexo,
       - fecha nac o edad (según contrato),
       - peso (numérico),
       - esterilizado (bool),
       - alergias/alertas (texto),
       - antecedentes (texto),
       - código interno (único por sucursal; BRD-REQ-019).
     - Owner fijo:
       - mascota se crea desde la ficha de un cliente,
       - al editar, NO permitir cambiar de propietario (BRD-REQ-020) aunque backend lo permita (v1).
   - Validación UI mínima:
     - identificación: opcional; si se ingresa, validar “solo dígitos” y longitud 10 o 13 (cédula/RUC v1). Si backend define otra regla en OpenAPI (pattern), usar la del backend.
     - teléfono: permitir dígitos + `+` y espacios (no bloquear; solo mínimo).
     - email: patrón básico.
     - peso: número >= 0.
     - código interno: requerido si el backend lo requiere; si no lo requiere, permitir vacío pero mostrar recomendación (sin inventar).
   - Conflictos:
     - si backend devuelve 409 por código interno duplicado: mostrar “Conflicto: el código interno ya existe en esta sucursal”.

6) **Rutas y navegación (sin asumir estructura previa, pero consistente)**
   - Implementar con paths consistentes con el proyecto:
     - Si ya existe convención de rutas en español, mantenerla (ej. `/clientes`).
     - Si convención existente es en inglés, mantenerla (ej. `/clients`).
   - Añadir ítem “Clientes” en menú del shell.

7) **Cierre del sprint**
   - Ejecutar comandos “verdad” (ver sección 8).
   - Actualizar:
     - `docs/log/log.md` (append-only) con outputs o placeholders
     - `docs/status/status.md` → `READY_FOR_VALIDATION` con hash commit
     - `docs/traceability/rtm.md` para BRD-REQ-016..020 → evidencia del commit
     - `docs/state/state.md` snapshot y siguiente sprint recomendado
   - Commit: `SPR-F003: clientes + mascotas CRUD + búsqueda`

## 6) Criterios de aceptación (AC)

- [ ] Existe módulo “Clientes” accesible desde navegación (según permisos).
- [ ] Lista de clientes carga desde backend real y soporta búsqueda (según contrato real).
- [ ] Crear cliente funciona y se refleja en lista/ficha.
- [ ] Editar cliente funciona.
- [ ] Ficha de cliente muestra mascotas asociadas (según contrato real).
- [ ] Crear mascota desde un cliente funciona.
- [ ] Editar mascota funciona sin permitir cambiar propietario (v1).
- [ ] Validaciones mínimas UI:
  - [ ] identificación opcional; si existe valida formato básico (o el pattern real del backend si está definido).
  - [ ] peso numérico >= 0.
- [ ] Manejo de errores consistente (Problem Details) y mensajes humanos en español.
- [ ] `npm run build` (o equivalente real del frontend) pasa.
- [ ] `docs/status/status.md` actualizado a `READY_FOR_VALIDATION` con evidencia (commit hash).
- [ ] `docs/log/log.md` append-only con sección SPR-F003 y placeholders/outputs.
- [ ] `docs/traceability/rtm.md` actualizado para BRD-REQ-016..020 apuntando a SPR-F003.
- [ ] `docs/state/state.md` actualizado (snapshot).

## 7) Smoke test manual (usuario)

1) Levantar backend + PostgreSQL local según `docs/08-runbook.md` y asegurar seed demo (B011).
2) Levantar frontend:
   - `cd <FRONT_DIR>`
   - `npm install` (o el comando real según lockfile)
   - `npm run dev`
3) En navegador:
   - Login con credenciales demo
   - Ir a “Clientes”
   - Buscar un cliente seed por nombre/teléfono/identificación (según existan)
   - Crear un cliente nuevo y verificar que aparece
   - Entrar a ficha y agregar una mascota
   - Editar mascota (peso/código interno) y verificar guardado

**Evidencia:** PEGAR OUTPUT EN `docs/log/log.md` en la entrada de SPR-F003.

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
